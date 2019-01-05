package XsltTransformer;

public class SerializationProperties {
    private String encoding;
    private String mime;

    public SerializationProperties(String mime, String encoding){
        this.encoding = encoding;
        this.mime = mime;
    }

    public String contentType(){
        StringBuilder sb = new StringBuilder();
        sb.append(mime == null ? "text/html" : mime);
        if(encoding != null){
            sb.append("; charset=" + encoding);
        }
        return sb.toString();
    }
}
