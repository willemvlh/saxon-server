package io.github.willemvlh.morph.saxon;

public class TransformationException extends Exception {

    public TransformationException(String message, Throwable e) {
        super(message, e);
    }

    public TransformationException(String msg) {
        super(msg);
    }

}
