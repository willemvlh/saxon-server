package XsltTransformer;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.sf.saxon.s9api.SaxonApiException;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Modifier;

import static spark.Spark.*;

public class Server {

    private final String ENDPOINT = "/transform";
    private final String INPUT_KEY = "xml";
    private final String XSL_KEY = "xsl";

    public static void main(String[] args){
        Server s = new Server();
        s.configureRoutes();
    }

    public void configureRoutes(){
        port(5000);
        post(ENDPOINT, (req,res) -> {
            try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
                req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
                Part xmlPart = req.raw().getPart(INPUT_KEY);
                if(xmlPart == null){
                    return handleException(res, 400, new InvalidRequest("No XML input found")).body();
                }
                try(InputStream input = xmlPart.getInputStream())
                {
                    Part xslPart = req.raw().getPart(XSL_KEY);
                    if(xslPart == null){
                        return handleException(res, 400, new InvalidRequest("No XSL input found")).body();
                    }
                    SaxonTransformer transformer = new SaxonTransformer();
                    try(InputStream stylesheet = xslPart.getInputStream()){
                        SerializationProperties props = transformer.transform(input, stylesheet, outputStream);
                        res.raw().setContentType(props.contentType());
                        outputStream.writeTo(res.raw().getOutputStream());
                        res.raw().getOutputStream().flush();
                        res.raw().getOutputStream().close();
                        return res.raw();
                    }
                    catch(SaxonApiException saxEx){
                        return handleException(res, 400, saxEx).body();
                    }
                }

                catch (Exception e){
                    Response response = handleException(res, 500, e);
                    return response.body();
                }
            }
        });
    }

    private static Response handleException(Response res, int status, Exception e){
        Error err = new Error(res, e, status);
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

