package tv.mediagenix.xslt.transformer.server;

import org.apache.commons.cli.ParseException;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;
import tv.mediagenix.xslt.transformer.saxon.core.TransformationException;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;

import static spark.Spark.*;

public class Server {

  private static final Logger logger = (Logger) LoggerFactory.getLogger(Server.class);
  private static ServerOptions options;

  public static ServerOptions getOptions() {
    return options;
  }

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
    post("/transform", (req, res) -> new XSLTRequestHandler(req, res).handle());
    post("/query", (req, res) -> new XQueryResultHandler(req, res).handle());
    get("/info", (Request req, Response res) -> {
      return ServerInfo.getInstance().load();
    }, new JsonTransformer());
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
