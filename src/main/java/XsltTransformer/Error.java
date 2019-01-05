package XsltTransformer;

import spark.Response;

public class Error{
    public int statusCode;
    public String exceptionType;
    public String message;
    private Response res;

    Error(Response res, Exception e, int statusCode) {
        this.message = e.getMessage();
        this.statusCode = statusCode;
        this.res = res;
        this.exceptionType = e.getClass().getSimpleName();
    }


}
