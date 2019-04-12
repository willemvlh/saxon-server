package XsltTransformer;

import net.sf.saxon.s9api.SaxonApiException;
import spark.Response;

public class ErrorMessage {
    public int statusCode;
    public String exceptionType;
    public String message;
    private Response res;

    ErrorMessage(Response res, Throwable e, int statusCode) {
        this.message = e.getMessage();
        this.statusCode = statusCode;
        this.res = res;
        this.exceptionType = e.getClass().getSimpleName();
    }


}
