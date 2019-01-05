package XsltTransformer;
import net.sf.saxon.s9api.*;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.OutputStream;

public class SaxonTransformer implements XsltTransformer {
    public SerializationProperties transform(InputStream input, InputStream stylesheet, OutputStream output) throws SaxonApiException {
        Processor p = new Processor(false);
        XsltCompiler c = p.newXsltCompiler();
        XsltExecutable e = c.compile(new StreamSource(stylesheet));
        Xslt30Transformer xf = e.load30();
        Serializer s = xf.newSerializer(output);
        xf.transform(new StreamSource(input), s);
        SerializationProperties props = new SerializationProperties(s.getOutputProperty(Serializer.Property.MEDIA_TYPE), s.getOutputProperty(Serializer.Property.ENCODING));
        return props;
    }
}
