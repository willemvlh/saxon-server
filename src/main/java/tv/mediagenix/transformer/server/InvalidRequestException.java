package tv.mediagenix.transformer.server;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(Exception e) {
        super(e);
    }
}
