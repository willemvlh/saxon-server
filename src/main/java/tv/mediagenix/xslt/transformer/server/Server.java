package tv.mediagenix.xslt.transformer.server;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;
import tv.mediagenix.xslt.transformer.saxon.SerializationProps;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;
import tv.mediagenix.xslt.transformer.saxon.actors.*;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import static spark.Spark.*;

public class Server {

    private final Logger logger = LoggerFactory.getLogger(Server.class);
    private ServerOptions options;
    private HttpServletRequest request;

    public static void main(String[] args) {
        try {
            ServerOptions options = ServerOptions.fromArgs(args);
            Server.newServer(options);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    private Server(ServerOptions options) {
        if (options == null) {
            this.options = new ServerOptions();
        } else {
            this.options = options;
        }
    }

    public static Server newServer(ServerOptions options) {
        Server s = new Server(options);
        s.configureKeystore();
        s.configureRoutes();
        s.configureExceptions();
        s.configureFilters();
        return s;
    }

    private void configureFilters() {
        before("/*", (req, res) -> {
            if (!isAuthenticated()) {
                halt(401);
            }
        });
        before("/*", (req, res) -> this.request = req.raw());
        before("/*", (req, res) -> res.raw().setHeader("Server", "/"));
    }

    private boolean isAuthenticated() {
        return true;
    }

    private void configureExceptions() {
        exception(InvalidRequestException.class, (e, req, res) -> Server.handleException(e, res, 400));
        exception(TransformationException.class, (e, req, res) -> Server.handleException(e.getCause() != null ? e.getCause() : e, res, 400));
        exception(Exception.class, (e, req, res) -> Server.handleException(e, res, 500));
        notFound((req, res) -> {
            res.type("application/json");
            res.status(404);
            return ("\"404 Not Found\"");
        });
    }

    private void configureKeystore() {
        String keyStoreProp = System.getProperty("keystore");
        if (keyStoreProp != null) {
            String keyStorePassw = System.getProperty("keystorePassword");
            if (keyStorePassw == null) keyStorePassw = "";
            secure(keyStoreProp, keyStorePassw, null, null);
        }
    }

    private void configureRoutes() {
        port(options.getPort());
        post("/transform", this::handleXsltRequest);
        post("/query", this::handleXQueryRequest);
    }

    private Object handleXQueryRequest(Request req, Response res) throws Exception {
        return handleRequest(req, res, ActorType.QUERY);
    }

    private Object handleXsltRequest(Request req, Response res) throws Exception {
        return handleRequest(req, res, ActorType.TRANSFORM);
    }

    private Object handleRequest(Request req, Response res, ActorType actorType) throws Exception {
        long startTime = System.currentTimeMillis();
        req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("saxon"));
        Optional<InputStream> input = getStreamFromRequestByKey(req, "xml");
        try (InputStream stylesheet = getStreamFromRequestByKey(req, "xsl").orElseThrow(() -> new InvalidRequestException("No XSL attachment found"))) {
            SaxonActor actor = getActorFromBuilder(newBuilder(actorType));
            ByteArrayOutputStream writeStream = new ByteArrayOutputStream();
            SerializationProps props = input.isPresent() ? actor.act(input.get(), stylesheet, writeStream) : actor.act(stylesheet, writeStream);
            res.header("Content-Type", props.getContentType());
            writeStream.writeTo(res.raw().getOutputStream());
            res.raw().getOutputStream().close();
            return res;
        } finally {
            logger.info(String.format("Finished request in %s milliseconds", System.currentTimeMillis() - startTime));
        }
    }

    private SaxonActor getActorFromBuilder(SaxonActorBuilder builder) {
        try {
            return builder.setInsecure(this.options.isInsecure()).setConfigurationFile(options.getConfigFile()).setSerializationProperties(getSerializationParams(request.getPart("output"))).build();
        } catch (Exception e) {
            throw new InvalidRequestException(e);
        }
    }

    private Map<String, String> getSerializationParams(Part part) {
        if (part == null) return new HashMap<>();
        try {
            InputStream s = part.getInputStream();
            return new ParameterParser().parseStream(s, (int) part.getSize());
        } catch (IOException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    private Optional<InputStream> getStreamFromRequestByKey(Request req, String key) throws IOException {
        try {
            Part part = req.raw().getPart(key);
            return part == null ? Optional.empty() : getStreamFromPart(part);
        } catch (ServletException e) {
            throw new InvalidRequestException(String.format("Could not read parts for key \"%s\" - did you forget to attach a file? (%s)", key, e.getMessage()));
        }
    }

    private Optional<InputStream> getStreamFromPart(Part part) throws IOException {
        String contentType = part.getContentType();
        if (contentType != null && "application/gzip".equals(contentType.toLowerCase())) {
            return Optional.of(getZippedStreamFromPart(part.getInputStream()));
        }
        return Optional.ofNullable(part.getInputStream());
    }

    private SaxonActorBuilder newBuilder(ActorType type) {
        switch (type) {
            case TRANSFORM:
                return new SaxonTransformerBuilder();
            case QUERY:
                return new SaxonXQueryPerformerBuilder();
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    private InputStream getZippedStreamFromPart(InputStream input) {
        try {
            GZIPInputStream s = new GZIPInputStream(input);
            ZippedStreamReader r = new ZippedStreamReader();
            return r.unzipStream(s);
        } catch (IOException e) {
            throw new InvalidRequestException(e);
        }
    }

    private static Response handleException(Throwable e, Response res, int status) {
        ErrorMessage err = new ErrorMessage(e, status);
        res.status(status);
        res.type("application/json;charset=utf-8");
        res.body(new JsonTransformer().render(err));
        return res;
    }

    public void stop() {
        Spark.stop();
    }
}

