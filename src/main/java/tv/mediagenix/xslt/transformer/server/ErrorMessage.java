package tv.mediagenix.xslt.transformer.server;

public class ErrorMessage {
    @SuppressWarnings("unused")
    private int statusCode;
    @SuppressWarnings("unused")
    private String exceptionType;
    @SuppressWarnings("unused")
    private String message;

    public ErrorMessage(Throwable e, int statusCode) {
        this.message = e.getMessage();
        this.statusCode = statusCode;
        this.exceptionType = e.getClass().getSimpleName();
    }
}
