package XsltTransformer;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.sf.saxon.s9api.SaxonApiException;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Modifier;

import static spark.Spark.*;

public class Server {

    public void configureRoutes(){
        port(5000);
        get("/test", (req,res) -> {
            System.out.println(req.ip());
            return req.toString();
        } );
        post("/test", (req,res) -> {
            try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
                req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
                try(InputStream input = req.raw().getPart("file1").getInputStream())
                {
                    try(InputStream stylesheet = req.raw().getPart("file2").getInputStream()){
                        SaxonTransformer transformer = new SaxonTransformer();
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
                catch (ServletException e){
                    String msg = e.getMessage() == null ? "Error when handling input" : e.getMessage();
                    Response response = handleException(res, 400, new InvalidRequest(msg));
                    return response.body();
                }
                catch (Exception e){
                    Response response = handleException(res, 500, e);
                    return response.body();
                }
            }
        });
    }

    public static void main(String[] args){
        Server s = new Server();
        s.configureRoutes();
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

