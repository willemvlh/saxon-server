package tv.mediagenix.xslt.transformer.server;

public class InvalidRequestException extends Exception {
    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(Exception e) {
        super(e);
    }
}
