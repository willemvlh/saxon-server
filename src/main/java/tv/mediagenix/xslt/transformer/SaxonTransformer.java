package tv.mediagenix.xslt.transformer;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.*;
import net.sf.saxon.trans.XPathException;
import org.slf4j.LoggerFactory;

import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SaxonTransformer implements XsltTransformer {

    public SaxonTransformer(File config) throws TransformationException {
        if (config != null) {
            try {
                this.config = Configuration.readConfiguration(new StreamSource(config));
            } catch (XPathException e) {
                LoggerFactory.getLogger(this.getClass()).error(e.getMessage());
                throw new TransformationException(e);
            }
        }
        this.setProcessor(newProcessor());
    }

    public SaxonTransformer(boolean insecure) throws TransformationException {
        this(null);
        this.configurationFactory = insecure ? new SaxonDefaultConfigurationFactory() : new SaxonSecureConfigurationFactory();
    }

    public SaxonTransformer() throws TransformationException {
        this(null);
    }

    private Processor processor;
    private SaxonConfigurationFactory configurationFactory = new SaxonSecureConfigurationFactory();
    private Configuration config;

    /**
     * When errors occur during the transformation, they are stored in this list.
     */
    private ArrayList<StaticError> errorList = new ArrayList<>();

    /**
     * @return The error list
     */
    public List<StaticError> getErrorList() {
        return errorList;
    }

    /**
     * @param input      The input XML stream
     * @param stylesheet The input stylesheet stream
     * @param output     The stream to which the output should be written
     * @return The serialization properties of the transformation
     * @throws TransformationException
     */
    public SerializationProperties transform(InputStream input, InputStream stylesheet, OutputStream output) throws TransformationException {
        SaxonMessageListener ml = new SaxonMessageListener();
        try {
            Xslt30Transformer xf = newTransformer(stylesheet);
            xf.setMessageListener(ml);
            Serializer s = xf.newSerializer(output);
            xf.transform(newSAXSource(input), s);
            return new SerializationProperties(s.getOutputProperty(Serializer.Property.MEDIA_TYPE), s.getOutputProperty(Serializer.Property.ENCODING));
        } catch (SaxonApiException e) {
            String msg = ml.errorString != null ? ml.errorString : e.getMessage();
            LoggerFactory.getLogger(this.getClass()).error(msg);
            throw new TransformationException(msg);
        }

    }

    Xslt30Transformer newTransformer(InputStream stylesheet) throws SaxonApiException {
        Processor p = getProcessor();
        XsltCompiler c = p.newXsltCompiler();
        c.setErrorList(errorList);
        XsltExecutable e = c.compile(newSAXSource(stylesheet));
        return e.load30();
    }

    private SAXSource newSAXSource(InputStream stream) {
        return this.configurationFactory.newSAXSource(stream);
    }

    private Processor newProcessor() {
        Configuration config = this.config == null ? configurationFactory.newConfiguration() : this.config;
        return new Processor(config);
    }

    Processor getProcessor() {
        return processor;
    }

    void setProcessor(Processor processor) {
        this.processor = processor;
    }
}
