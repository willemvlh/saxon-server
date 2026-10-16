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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;

public class Server {

  private static final Logger logger = (Logger) LoggerFactory.getLogger(Server.class);
  private static ServerOptions options;

  public static ServerOptions getOptions() {
    return options;
  }

  public static void main(String[] args) {
    try {
      options = ServerOptions.fromArgs(args);
      logStartupInfo();
      setUp();
    } catch (ParseException | InvalidOptionException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    }
  }

  private static void logStartupInfo() {
    var byteArrayOutputStream = new ByteArrayOutputStream();
    var printStream = new PrintStream(byteArrayOutputStream);
    ServerInfo.getInstance().load().print(printStream);
    logger.info("\n"+byteArrayOutputStream.toString());
    logger.info("JVM: {} {} ({}), Java {} at {}",
        System.getProperty("java.vm.name"),
        System.getProperty("java.vm.version"),
        System.getProperty("java.vm.vendor"),
        System.getProperty("java.version"),
        System.getProperty("java.home"));
    logger.debug("JVM arguments: {}", ManagementFactory.getRuntimeMXBean().getInputArguments());
    logger.debug("\nOptions: \n{}", options);
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
    Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(Level.INFO); // avoid verbose Jetty messages
    Logger ourLogger = (Logger) LoggerFactory.getLogger("tv.mediagenix");
    ourLogger.setLevel(options.isDebuggingEnabled() ? Level.DEBUG : Level.INFO);

  }

  private static void configureFilters() {
    before("/*", (req, res) -> {
      logger.info("Received request from {} (content-length={})", req.ip(),
          req.contentLength());
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
    if (logger.isDebugEnabled()) {
      logger.debug("Invalid request:");
      logger.debug("IP: {}", req.ip());
      logger.debug("Content-Type: {}", req.contentType());
    }
  }

  private static void configureRoutes() {
    port(options.getPort());
    post("/transform", (req, res) -> new XSLTRequestHandler(req, res).handle());
    post("/query", (req, res) -> new XQueryResultHandler(req, res).handle());
    get("/info", (Request req, Response res) -> {
      res.header("Content-Type", "application/json;charset=utf-8");
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
