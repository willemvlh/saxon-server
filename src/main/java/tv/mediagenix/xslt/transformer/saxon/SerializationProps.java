package tv.mediagenix.xslt.transformer.saxon;

public class SerializationProps {
    private String encoding;
    private String mime;
    private final String defaultEncoding = "utf-8";
    private final String defaultMime = "application/xml";

    public String getEncoding() {
        return encoding == null ? defaultEncoding : encoding;
    }

    public String getMime() {
        return mime == null ? getDefaultMime() : mime;
    }

    protected String getDefaultMime() {
        return defaultMime;
    }

    public SerializationProps(String mime, String encoding) {
        this.encoding = encoding;
        this.mime = mime;
    }

    public String getContentType() {
        return getMime() + ";charset=" + getEncoding();
    }
}
