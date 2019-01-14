package XsltTransformer;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;

import static spark.Spark.port;
import static spark.Spark.post;

public class Server {

    private final String ENDPOINT = "/transform";
    private final String INPUT_KEY = "xml";
    private final String XSL_KEY = "xsl";
    private Logger logger = LoggerFactory.getLogger(Server.class);
    private Integer requestCount = 0;
    private Integer finishedRequestCount = 0;


    public static void main(String[] args){
        Server s = new Server();
        s.configureRoutes();
    }

    public void configureRoutes(){
        port(getPort());
        post(ENDPOINT, (req,res) -> {
            requestCount++;
            logger.info(String.format("Received a request from %s (#%s)", req.ip(), requestCount.toString()));
            try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
                req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
                try(InputStream input = getStreamFromPart(req.raw().getPart(INPUT_KEY))) {
                    try (InputStream stylesheet = getStreamFromPart(req.raw().getPart(XSL_KEY))) {
                        SaxonTransformer tf = new SaxonTransformer();
                        tf.transform(input, stylesheet, outputStream);
                        return outputStream;
                    }
                }
            }
            catch(TransformationException e){
                return handleException(res, 400, e).body();
            }
            catch (Exception e){
                return handleException(res, 500, e).body();
            }
            finally {
                logger.info(String.format("Finished request #%s", requestCount.toString()));
            }
        });
    }

    private InputStream getStreamFromPart(Part part) throws InvalidRequestException, IOException {
        if(part == null){
            throw new InvalidRequestException("No input found for file with key: " + part.getName());
        }
        return part.getInputStream();
    }

    private int getPort(){
        String systemEnvPort = System.getenv("PORT");
        String javaPropPort = System.getProperty("port");

        if(systemEnvPort != null){
            return Integer.valueOf(systemEnvPort);
        }
        if(javaPropPort != null){
            return Integer.valueOf(javaPropPort);
        }
        return 5000;

    }

    private static Response handleException(Response res, int status, Exception e){
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

