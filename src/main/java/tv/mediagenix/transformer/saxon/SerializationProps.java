package tv.mediagenix.transformer.saxon;

public class SerializationProps {
    private final String encoding;
    private final String mime;

    public String getEncoding() {
        return encoding == null ? "utf-8" : encoding;
    }

    public String getMime() {
        return mime == null ? getDefaultMime() : mime;
    }

    protected String getDefaultMime() {
        return "application/xml";
    }

    public SerializationProps(String mime, String encoding) {
        this.encoding = encoding;
        this.mime = mime;
    }

    public String getContentType() {
        return getMime() + ";charset=" + getEncoding();
    }
}
