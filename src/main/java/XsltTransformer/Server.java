package XsltTransformer;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.Request;
import spark.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;

import static spark.Spark.port;
import static spark.Spark.post;

public class Server {

    private final String ENDPOINT = "/transform";
    private final String INPUT_KEY = "xml";
    private final String XSL_KEY = "xsl";
    private Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        Server s = new Server();
        s.configureRoutes();
    }

    public void configureRoutes() {
        port(getPort());
        post(ENDPOINT, (req, res) -> {
            long startTime = System.currentTimeMillis();
            logger.info(String.format("Received a request from %s at %s", req.ip(), LocalDateTime.now()));
            req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
            try (InputStream input = getStreamFromRequestByKey(req, INPUT_KEY)) {
                try (InputStream stylesheet = getStreamFromRequestByKey(req, XSL_KEY)) {
                    res.header("Content-type", "bla");
                    SaxonTransformer tf = new SaxonTransformer();
                    ByteArrayOutputStream writeStream = new ByteArrayOutputStream();
                    SerializationProperties props = tf.transform(input, stylesheet, writeStream);
                    res.header("Content-type", props.contentType());
                    writeStream.writeTo(res.raw().getOutputStream());
                    return "";
                }
            } catch (InvalidRequestException e) {
                return handleException(res, 400, e);
            } catch (TransformationException e) {
                return handleException(res, 400, e.getCause()).body();
            } catch (Exception e) {
                return handleException(res, 500, e).body();
            } finally {
                logger.info(String.format("Finished request in %s milliseconds", System.currentTimeMillis() - startTime));
            }
        });
    }

    private InputStream getStreamFromRequestByKey(Request req, String key) throws InvalidRequestException, IOException, ServletException {
        try {
            Part part = req.raw().getPart(key);
            return getStreamFromPart(part);
        } catch (NullPointerException e) {
            throw new InvalidRequestException(String.format("No part found for key \"%s\"", key));
        }
    }

    private InputStream getStreamFromPart(Part part) throws IOException, InvalidRequestException {
        if (part == null) {
            throw new InvalidRequestException(String.format("No input found for file with key \"%s\"", part.getName()));
        }
        return part.getInputStream();
    }

    private int getPort() {
        String systemEnvPort = System.getenv("PORT");
        String javaPropPort = System.getProperty("port");

        if (systemEnvPort != null) {
            return Integer.valueOf(systemEnvPort);
        }
        if (javaPropPort != null) {
            return Integer.valueOf(javaPropPort);
        }
        return 5000;

    }

    private static Response handleException(Response res, int status, Throwable e) {
        ErrorMessage err = new ErrorMessage(res, e, status);
        res.status(status);
        res.type("application/json");
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                return fieldAttributes.hasModifier(Modifier.PRIVATE);
            }

            @Override
            public boolean shouldSkipClass(Class<?> aClass) {
                return false;
            }
        });
        Gson gson = gsonBuilder.create();
        res.body(gson.toJson(err));
        return res;
    }

}

