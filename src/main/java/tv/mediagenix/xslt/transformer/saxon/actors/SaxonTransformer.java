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
    public SerializationProps act(XdmValue input, InputStream stylesheet, OutputStream output) throws TransformationException {
        Xslt30Transformer transformer = newTransformer(newSAXSource(stylesheet));
        Serializer s = transformer.newSerializer(output);
        try {
            transformer.setStylesheetParameters(this.getParameters());
            if (input.isEmpty()) {
                //no input, use default template "xsl:initial-template"
                transformer.callTemplate(null, s);
            } else {
                //apply templates on context item
                transformer.applyTemplates(input, s);
            }
            return getSerializationProperties(s);
        } catch (SaxonApiException e) {
            SaxonMessageListener listener = (SaxonMessageListener) transformer.getMessageListener2();
            String msg = listener.errorString != null ? listener.errorString : e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
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
