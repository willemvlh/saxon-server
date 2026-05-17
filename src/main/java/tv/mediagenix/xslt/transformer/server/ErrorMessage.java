package tv.mediagenix.xslt.transformer.server;

public class ErrorMessage {
    private int statusCode;
    private String exceptionType;
    private String message;

    public ErrorMessage(Throwable e, int statusCode) {
        this.message = e.getMessage();
        this.statusCode = statusCode;
        this.exceptionType = e.getClass().getSimpleName();
    }
}
