package tv.mediagenix.xslt.transformer.saxon.actors;

import net.sf.saxon.s9api.*;
import org.slf4j.LoggerFactory;
import tv.mediagenix.xslt.transformer.saxon.SaxonMessageListener;
import tv.mediagenix.xslt.transformer.saxon.SerializationProps;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;

import javax.xml.transform.Source;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SaxonTransformer extends SaxonActor {

    private ArrayList<StaticError> errorList = new ArrayList<>();

    public List<StaticError> getErrorList() {
        return errorList;
    }

    @Override
    public SerializationProps act(InputStream input, InputStream stylesheet, OutputStream output, XdmItem contextItem) throws TransformationException {
        return transform(input != null ? newSAXSource(input) : null, newSAXSource(stylesheet), output, contextItem);
    }

    @Override
    public SerializationProps act(InputStream stylesheet, OutputStream output) throws TransformationException {
        return transform(null, newSAXSource(stylesheet), output, null);
    }

    private SerializationProps transform(Source input, Source stylesheet, OutputStream output, XdmItem contextItem) throws TransformationException {
        Xslt30Transformer transformer = newTransformer(stylesheet);
        Serializer s = transformer.newSerializer(output);
        try {
            if (contextItem != null) {
                //skip the input source, apply transformation on the context item
                transformer.applyTemplates(contextItem, s);
            } else if (input == null) {
                //no input, use default template "xsl:initial-template"
                transformer.callTemplate(null, s);
            } else {
                //regular transformation with xml input source
                transformer.transform(input, s);
            }
            return getSerializationProperties(s);
        } catch (SaxonApiException e) {
            SaxonMessageListener listener = (SaxonMessageListener) transformer.getMessageListener2();
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
            Xslt30Transformer xf = e.load30();
            xf.setMessageListener(new SaxonMessageListener());
            return xf;
        } catch (SaxonApiException e) {
            if (this.getErrorList().size() > 0) {
                StaticError error = this.getErrorList().get(0);
                String message;
                if (error instanceof XmlProcessingError && ((XmlProcessingError) error).getCause() != null) {
                    message = ((XmlProcessingError) error).getCause().getMessage();
                    //will usually contain a parsing error
                } else {
                    message = error.getMessage();
                }
                throw new TransformationException("Compilation error: " + message + " (line " + error.getLineNumber() + ", col " + error.getColumnNumber() + ")");
            }
            throw new TransformationException(e);
        }
    }
}
