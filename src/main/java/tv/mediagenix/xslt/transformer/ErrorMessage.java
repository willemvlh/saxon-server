package tv.mediagenix.xslt.transformer;

public class ErrorMessage {
    public int statusCode;
    public String exceptionType;
    public String message;

    public ErrorMessage(Throwable e, int statusCode) {
        this.message = e.getMessage();
        this.statusCode = statusCode;
        this.exceptionType = e.getClass().getSimpleName();
    }

    public String toJson() {
        return new JsonTransformer().render(this);
    }


}
