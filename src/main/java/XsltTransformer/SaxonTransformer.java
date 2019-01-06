package XsltTransformer;
import net.sf.saxon.lib.StandardErrorListener;
import net.sf.saxon.s9api.*;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SaxonTransformer implements XsltTransformer {
    private ArrayList<StaticError> errorList = new ArrayList<>();

    public List<StaticError> getErrorList(){
        return errorList;
    }

    public SerializationProperties transform(InputStream input, InputStream stylesheet, OutputStream output) throws SaxonApiException {
        Processor p = new Processor(false);
        XsltCompiler c = p.newXsltCompiler();
        c.setErrorList(errorList);
        XsltExecutable e = c.compile(new StreamSource(stylesheet));
        Xslt30Transformer xf = e.load30();
        Serializer s = xf.newSerializer(output);
        xf.transform(new StreamSource(input), s);
        SerializationProperties props = new SerializationProperties(s.getOutputProperty(Serializer.Property.MEDIA_TYPE), s.getOutputProperty(Serializer.Property.ENCODING));
        return props;
    }
}
