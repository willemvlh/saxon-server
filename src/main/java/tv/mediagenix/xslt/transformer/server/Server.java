package tv.mediagenix.xslt.transformer.server;

import org.apache.commons.cli.ParseException;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;
import tv.mediagenix.xslt.transformer.saxon.SerializationProps;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;
import tv.mediagenix.xslt.transformer.saxon.actors.ActorType;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonActor;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonActorBuilder;
import tv.mediagenix.xslt.transformer.server.ratelimiter.RateLimiter;

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
import java.util.zip.GZIPInputStream;

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
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    public static void setUp() {
        configureLogger();
        configureKeystore();
        configureRoutes();
        configureExceptions();
        configureFilters();
    }

    private static void configureLogger() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO); //avoid verbose Jetty messages
        Logger ourLogger = (Logger) LoggerFactory.getLogger("tv.mediagenix");
        ourLogger.setLevel(options.isDebuggingEnabled() ? Level.DEBUG : Level.INFO);
        logger.debug("Options: {}", options);
    }

    private static void configureFilters() {
        before("/*", (req, res) -> {
            RateLimiter rl = options.getRateLimiter();
            String ip = req.ip();
            if (rl.canRequest(ip)) {
                rl.registerRequest(ip);
            } else {
                long seconds = rl.timeToAllowed(ip).getSeconds();
                halt(403, "Rate limit exceeded - wait " + seconds + " seconds.");
            }
        });
        before("/*", (req, res) -> logger.info("Received request from {} (session-id = {}, content-length={})", req.ip(),req.session().id(), req.contentLength()));
        before("/*", (req, res) -> res.raw().setHeader("Server", "/"));
    }

    private static void configureExceptions() {
        exception(InvalidRequestException.class, (e, req, res) -> {
            Server.handleException(e, res, 400);
            logRequest(req);
        });
        exception(TransformationException.class, (e, req, res) -> Server.handleException(e, res, 400));
        exception(Exception.class, (e, req, res) -> Server.handleException(e, res, 500));
        notFound((req, res) -> {
            res.type("application/json");
            res.status(404);
            return ("\"404 Not Found\"");
        });
    }

    private static void logRequest(Request req) {
        logger.debug("Invalid request:");
        logger.debug("IP: {}", req.ip());
        logger.debug("Content-Type: {}", req.contentType());
        try {
            Collection<Part> parts = req.raw().getParts();
            parts.forEach(part -> {
                logger.debug("Part: type={}, name={}, size={}", part.getContentType(), part.getName(), part.getSize());
            });
        } catch (Exception e) {
            logger.debug("Could not read parts: {}", e.getMessage());
        }

    }

    private static void configureKeystore() {
        String keyStoreProp = System.getProperty("keystore");
        if (keyStoreProp != null) {
            String keyStorePassw = System.getProperty("keystorePassword");
            if (keyStorePassw == null) keyStorePassw = "";
            secure(keyStoreProp, keyStorePassw, null, null);
        }
    }

    private static void configureRoutes() {
        port(options.getPort());
        post("/transform", Server::handleXsltRequest);
        post("/query", Server::handleXQueryRequest);
    }

    private static Object handleXQueryRequest(Request req, Response res) throws Exception {
        handleRequest(req, res, ActorType.QUERY);
        return 0;
    }

    private static Object handleXsltRequest(Request req, Response res) throws Exception {
        handleRequest(req, res, ActorType.TRANSFORM);
        return 0;
    }

    private static void handleRequest(Request req, Response res, ActorType actorType) throws Exception {
        long startTime = System.currentTimeMillis();
        req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("saxon"));
        Optional<InputStream> input = getStreamFromRequestByKey(req, "xml");
        try (InputStream stylesheet = getStreamFromRequestByKey(req, "xsl").orElseThrow(() -> new InvalidRequestException("No XSL attachment found"))) {
            SaxonActor actor = getActorFromBuilder(SaxonActorBuilder.newBuilder(actorType), getParameters(req.raw().getPart("output")), getParameters(req.raw().getPart("parameters")));
            ByteArrayOutputStream writeStream = new ByteArrayOutputStream();
            SerializationProps props = input.isPresent() ? actor.act(input.get(), stylesheet, writeStream) : actor.act(stylesheet, writeStream);
            res.header("Content-Type", props.getContentType());
            writeStream.writeTo(res.raw().getOutputStream());
            res.raw().getOutputStream().close();
        }
        finally {
            logger.info("Finished request {} in {} milliseconds", req.session().id(), System.currentTimeMillis() - startTime);
        }
    }

    private static SaxonActor getActorFromBuilder(SaxonActorBuilder builder, Map<String, String> outputParameters, Map<String, String> parameters) {
        try {
            return builder
                    .setInsecure(options.isInsecure())
                    .setConfigurationFile(options.getConfigFile())
                    .setTimeout(options.getTransformationTimeoutMs())
                    .setSerializationProperties(outputParameters)
                    .setParameters(parameters)
                    .build();
        } catch (Exception e) {
            logger.error("Error: ", e);
            throw new InvalidRequestException(e);
        }
    }

    private static Map<String, String> getParameters(Part part) {
        if (part == null) return new HashMap<>();
        try {
            InputStream s = part.getInputStream();
            return new ParameterParser().parseStream(s, (int) part.getSize());
        } catch (IOException | IllegalArgumentException e) {
            logger.debug("Could not read parameters.", e);
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
            return getStreamFromPart(part);
        } catch (ServletException e) {
            throw new InvalidRequestException(String.format("Could not read parts for key \"%s\" - did you forget to attach a file? (%s)", key, e.getMessage()));
        }
    }

    private static Optional<InputStream> getStreamFromPart(Part part) throws IOException {
        String contentType = part.getContentType();
        if ("application/gzip".equalsIgnoreCase(contentType)) {
            logger.debug("Payload is zipped, attempting to  unzip...");
            return Optional.of(getZippedStreamFromPart(part.getInputStream()));
        }
        return Optional.ofNullable(part.getInputStream());
    }


    private static InputStream getZippedStreamFromPart(InputStream input) {
        try {
            GZIPInputStream s = new GZIPInputStream(input);
            ZippedStreamReader r = new ZippedStreamReader();
            return r.unzipStream(s);
        } catch (IOException e) {
            throw new InvalidRequestException(e);
        }
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

    public static void stop(){
        Spark.stop();
    }
}

