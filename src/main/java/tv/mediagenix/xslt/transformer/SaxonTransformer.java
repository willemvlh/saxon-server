package tv.mediagenix.xslt.transformer;
import net.sf.saxon.s9api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SaxonTransformer implements XsltTransformer {

    public SaxonTransformer(File config){
        if(config != null){
            this.config = new StreamSource(config);
        }
    }

    public SaxonTransformer(){
        this(null);
    }

    private Source config;
    /**
     * When errors occur during the transformation, they are stored in this list.
     */
    private ArrayList<StaticError> errorList = new ArrayList<>();

    /**
     *
     * @return The error list
     */
    public List<StaticError> getErrorList(){
        return errorList;
    }

    /**
     *
     * @param input The input XML stream
     * @param stylesheet The input stylesheet stream
     * @param output The stream to which the output should be written
     * @return The serialization properties of the transformation
     * @throws TransformationException
     */
    public SerializationProperties transform(InputStream input, InputStream stylesheet, OutputStream output) throws TransformationException {
        SaxonMessageListener ml = new SaxonMessageListener();
        try{
            Processor p = newProcessor();
            XsltCompiler c = p.newXsltCompiler();
            c.setErrorList(errorList);
            XsltExecutable e = c.compile(new StreamSource(stylesheet));
            Xslt30Transformer xf = e.load30();
            xf.setMessageListener(ml);
            Serializer s = xf.newSerializer(output);
            xf.transform(new StreamSource(input), s);
            return new SerializationProperties(s.getOutputProperty(Serializer.Property.MEDIA_TYPE), s.getOutputProperty(Serializer.Property.ENCODING));
        }
        catch(SaxonApiException e){
            Logger l = LoggerFactory.getLogger(Server.class);
            String msg = ml.errorString != null ? ml.errorString : e.getMessage();
            l.error(msg);
            throw new TransformationException(msg);
        }

    }

    private Processor newProcessor() throws SaxonApiException {
        if(this.config != null){
            LoggerFactory.getLogger(this.getClass()).info("Using config file: " + this.config.getSystemId());
            return new Processor(this.config);
        }
        return new Processor(false);
    }

}
