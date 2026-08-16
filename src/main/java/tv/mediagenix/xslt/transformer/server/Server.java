package tv.mediagenix.xslt.transformer.server;

import org.apache.commons.cli.ParseException;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonActor;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonActorBuilder;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonTransformerBuilder;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonXQueryPerformerBuilder;
import tv.mediagenix.xslt.transformer.saxon.core.SerializationProps;
import tv.mediagenix.xslt.transformer.saxon.core.TransformationException;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;

import static spark.Spark.*;

public class Server {

  private static final Logger logger = (Logger) LoggerFactory.getLogger(Server.class);
  private static ServerOptions options;

  public static void main(String[] args) {
    try {
      options = ServerOptions.fromArgs(args);
      setUp();
    } catch (ParseException | InvalidOptionException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    }
  }

  public static void setUp() {
    if (options.isDisableFrontend()) {
      logger.info("Frontend is disabled.");
    } else {
      staticFiles.location("/web");
    }
    configureLogger();
    configureRoutes();
    configureExceptions();
    configureFilters();
  }

  private static void configureLogger() {
    Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(Level.INFO); // avoid verbose Jetty messages
    Logger ourLogger = (Logger) LoggerFactory.getLogger("tv.mediagenix");
    ourLogger.setLevel(options.isDebuggingEnabled() ? Level.DEBUG : Level.INFO);
    logger.debug("Options: {}", options);
  }

  private static void configureFilters() {
    before("/*", (req, res) -> {
      logger.info("Received request from {} (session-id = {}, content-length={})", req.ip(),
          req.session().id(), req.contentLength());
      res.raw().setHeader("Server", "/");
    });
  }

  private static void logParts(Request req) {
    if (!logger.isDebugEnabled())
      return;
    try {
      Collection<Part> parts = req.raw().getParts();
      parts.forEach(part -> {
        logger.debug("Part: type={}, name={}, filename={}, size={}", part.getContentType(), part.getName(),
            part.getSubmittedFileName(), part.getSize());
        try {
          var buffer = getStreamFromPart(part).readAllBytes();
          logger.debug("Contents: {}", new String(buffer));
        } catch (IOException e) {
          logger.debug("Could not read part {}: {}", part.getName(), e.getMessage());
        }
      });
    } catch (Exception e) {
      logger.debug("Could not read parts: {}", e.getMessage());
    }
  }

  private static void configureExceptions() {
    exception(InvalidRequestException.class, (e, req, res) -> {
      Server.handleException(e, res, 400);
      logInvalidRequest(req);
    });
    exception(TransformationException.class, (e, req, res) -> Server.handleException(e, res, 400));
    exception(Exception.class, (e, req, res) -> Server.handleException(e, res, 500));
    notFound((req, res) -> {
      res.type("application/json");
      res.status(404);
      return ("\"404 Not Found\"");
    });
  }

  private static void logInvalidRequest(Request req) {
    logger.debug("Invalid request:");
    logger.debug("IP: {}", req.ip());
    logger.debug("Content-Type: {}", req.contentType());
  }

  private static void configureRoutes() {
    port(options.getPort());
    post("/transform", (req,res) -> new XSLTRequestHandler(req,res).handle());
    post("/query", Server::handleXQueryRequest);
    get("/info", (Request req, Response res) -> {
      return ServerInfo.getInstance().load();
    }, new JsonTransformer());
  }

  private static Object handleXQueryRequest(Request req, Response res) throws Exception {
    handleRequest(req, res, new SaxonXQueryPerformerBuilder());
    return 0;
  }

  private static Object handleXsltRequest(Request req, Response res) throws Exception {
    handleRequest(req, res, new SaxonTransformerBuilder());
    return 0;
  }

  private static void handleRequest(Request req, Response res, SaxonActorBuilder builder) throws Exception {
    long startTime = System.currentTimeMillis();
    req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/xxx/"));
    logParts(req);
    Optional<InputStream> input = getStreamFromRequestByKey(req, "xml");
    try (InputStream stylesheet = getStreamFromRequestByKey(req, "xsl")
        .orElseThrow(() -> new InvalidRequestException("No XSL attachment found"))) {
      SaxonActor actor = getActorFromBuilder(
          builder,
          getParameters(req.raw().getPart("output")),
          getParameters(req.raw().getPart("parameters")),
          getAdditionalFiles(req));
      ByteArrayOutputStream writeStream = new ByteArrayOutputStream();
      SerializationProps props = input.isPresent()
          ? actor.act(input.get(), stylesheet, writeStream)
          : actor.act(stylesheet, writeStream);
      res.header("Content-Type", props.getContentType());
      var outputStream = res.raw().getOutputStream();
      writeStream.writeTo(outputStream);
      outputStream.close();
    } finally {
      logger.info("Finished request {} in {} milliseconds", req.session().id(), System.currentTimeMillis() - startTime);
    }
  }

  private static Map<String, InputStream> getAdditionalFiles(Request req) {
    try {
      Collection<Part> parts = req.raw().getParts();
      return parts.stream()
          .filter(part -> part.getSubmittedFileName() != null && !part.getSubmittedFileName().isEmpty())
          .collect(Collectors.toMap(part -> part.getSubmittedFileName(), part -> {
            try {
              return getStreamFromPart(part);
            } catch (IOException e) {
              throw new InvalidRequestException(e);
            }
          }, (existing, incoming) -> {
            logger.warn("Duplicate file name found: {}. Using the first one.", existing);
            return existing;
          }));
    } catch (ServletException | IOException e) {
      throw new InvalidRequestException(e);
    }
  }

  private static SaxonActor getActorFromBuilder(SaxonActorBuilder builder, Map<String, String> outputParameters,
      Map<String, String> parameters, Map<String, InputStream> files) {
    try {
      return builder
          .setInsecure(options.isInsecure())
          .setBaseURI(options.getBaseURI())
          .setConfigurationFile(options.getConfigFile())
          .setTimeout(options.getTransformationTimeoutMs())
          .setSerializationProperties(outputParameters)
          .setParameters(parameters)
          .setFiles(files)
          .build();
    } catch (Exception e) {
      logger.error("Error during actor construction: ", e);
      throw new InvalidRequestException(e);
    }
  }

  private static Map<String, String> getParameters(Part part) {
    if (part == null)
      return new HashMap<>();
    try {
      InputStream s = part.getInputStream();
      return new ParameterParser().parseStream(s, (int) part.getSize());
    } catch (IOException | IllegalArgumentException e) {
      logger.error("Could not read parameters.", e);
      throw new InvalidRequestException(e.getMessage());
    }
  }

  private static Optional<InputStream> getStreamFromRequestByKey(Request req, String key) throws IOException {
    try {
      Part part = req.raw().getPart(key);
      if (part == null) {
        logger.debug("No part found named {} in request {}", key, req.session().id());
        return Optional.empty();
      }
      return Optional.ofNullable(getStreamFromPart(part));
    } catch (ServletException e) {
      throw new InvalidRequestException(String
          .format("Could not read parts for key \"%s\" - did you forget to attach a file? (%s)", key, e.getMessage()));
    }
  }

  private static InputStream getStreamFromPart(Part part) throws IOException {
    String contentType = part.getContentType();
    if ("application/gzip".equalsIgnoreCase(contentType)) {
      logger.debug("Payload is zipped");
      try {
        return new GZIPInputStream(part.getInputStream());
      } catch (ZipException e) {
        logger.error("Could not unzip payload: {}", e.getMessage());
        throw new InvalidRequestException("Could not unzip payload: " + e.getMessage());
      }
    }
    return part.getInputStream();
  }

  private static void handleException(Throwable e, Response res, int status) {
    logger.error("Error: {}", status, e);
    ErrorMessage err = new ErrorMessage(e, status);
    res.status(status);
    res.type("application/json;charset=utf-8");
    String body = new JsonTransformer().render(err);
    logger.info(body);
    res.body(body);
  }

  public static void stop() {
    Spark.stop();
  }
}
