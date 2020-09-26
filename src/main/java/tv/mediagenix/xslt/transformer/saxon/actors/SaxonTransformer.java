package tv.mediagenix.xslt.transformer.saxon.actors;

import net.sf.saxon.s9api.*;
import org.slf4j.LoggerFactory;
import tv.mediagenix.xslt.transformer.saxon.SaxonMessageListener;
import tv.mediagenix.xslt.transformer.saxon.SerializationProps;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;

import javax.xml.transform.Source;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SaxonTransformer extends SaxonActor {

    public SaxonTransformer(File config) throws TransformationException {
        super(config);
    }

    public SaxonTransformer(boolean b) {
        super(b);
    }

    public SaxonTransformer() {
        super();
    }

    private ArrayList<StaticError> errorList = new ArrayList<>();

    public List<StaticError> getErrorList() {
        return errorList;
    }

    @Override
    public SerializationProps act(InputStream input, InputStream stylesheet, OutputStream output) throws TransformationException {
        return transform(newSAXSource(input), newSAXSource(stylesheet), output);
    }

    @Override
    public SerializationProps act(InputStream stylesheet, OutputStream output) throws TransformationException {
        return transform(null, newSAXSource(stylesheet), output);
    }

    private SerializationProps transform(Source input, Source stylesheet, OutputStream output) throws TransformationException {
        SaxonMessageListener listener = new SaxonMessageListener();
        try {
            Xslt30Transformer transformer = newTransformer(stylesheet);
            Serializer s = transformer.newSerializer(output);
            transformer.setMessageListener(listener);
            if (input == null) {
                transformer.callTemplate(null, s);
            } else {
                transformer.transform(input, s);
            }
            return getSerializationProperties(s);
        } catch (SaxonApiException e) {
            String msg = listener.errorString != null ? listener.errorString : e.getMessage();
            LoggerFactory.getLogger(this.getClass()).error(msg);
            throw new TransformationException(msg);
        }
    }

    private Xslt30Transformer newTransformer(Source stylesheet) throws TransformationException {
        Processor p = getProcessor();
        XsltCompiler c = p.newXsltCompiler();
        c.setErrorList(this.getErrorList());
        try {
            XsltExecutable e = c.compile(stylesheet);
            return e.load30();
        } catch (SaxonApiException e) {
            if (this.getErrorList().size() > 0) {
                StaticError error = this.getErrorList().get(0);
                throw new TransformationException("Compilation error: " + error.getMessage() + " (line " + error.getLineNumber() + ", col " + error.getColumnNumber() + ")");
            }
            throw new TransformationException(e);
        }
    }
}
