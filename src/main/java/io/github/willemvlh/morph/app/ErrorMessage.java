package io.github.willemvlh.morph.app;

public class ErrorMessage {
    private int statusCode;
    private String exceptionType;
    private String message;

    public ErrorMessage(Throwable e, int statusCode) {
        this.message = e.getMessage();
        this.statusCode = statusCode;
        this.exceptionType = e.getClass().getSimpleName();
    }

    public ErrorMessage() {
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
