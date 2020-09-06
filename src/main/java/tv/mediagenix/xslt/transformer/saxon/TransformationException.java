package tv.mediagenix.xslt.transformer.saxon;

public class TransformationException extends Exception {
    public TransformationException(Exception e) {
        super(e);
    }

    public TransformationException(String msg) {
        super(msg);
    }

}
