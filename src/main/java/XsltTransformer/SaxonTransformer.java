package XsltTransformer;
import net.sf.saxon.lib.StandardErrorListener;
import net.sf.saxon.s9api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class SaxonTransformer implements XsltTransformer {
    private ArrayList<StaticError> errorList = new ArrayList<>();

    public List<StaticError> getErrorList(){
        return errorList;
    }

    public SerializationProperties transform(InputStream input, InputStream stylesheet, OutputStream output) throws TransformationException {
        try{
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
        catch(SaxonApiException e){
            Logger l = LoggerFactory.getLogger(Server.class);
            l.error(e.getMessage());
            throw new TransformationException(e);
        }

    }

}
