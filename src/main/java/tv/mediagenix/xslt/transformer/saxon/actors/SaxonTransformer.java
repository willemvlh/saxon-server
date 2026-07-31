package tv.mediagenix.xslt.transformer.saxon.actors;

import net.sf.saxon.s9api.*;
import net.sf.saxon.serialize.SerializationProperties;
import tv.mediagenix.xslt.transformer.saxon.core.SerializationProps;
import tv.mediagenix.xslt.transformer.saxon.core.TransformationException;

import javax.xml.transform.Source;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SaxonTransformer extends SaxonActor {

  private ArrayList<XmlProcessingError> errorList = new ArrayList<>();
  private Xslt30Transformer transformer;
  private String terminateMessage;

  public List<XmlProcessingError> getErrorList() {
    return errorList;
  }

  @Override
  public SerializationProps act(XdmValue input, InputStream stylesheet, OutputStream output)
      throws TransformationException {
    Source source = saxSourceFactory.newSAXSource(stylesheet);
    if (getBaseURI() != null) {
      source.setSystemId(getBaseURI().toString());
    }
    transformer = newTransformer(source);
    Serializer s = newSerializer(output);
    try {
      transformer.setStylesheetParameters(this.getParameters());
      if (input.isEmptySequence()) {
        // no input, use default template "xsl:initial-template"
        logger.debug("No XML input: defaulting to the default template");
        transformer.callTemplate(null, s);
      } else {
        // apply templates on context item
        transformer.setGlobalContextItem(input.itemAt(0));
        transformer.applyTemplates(input, s);
      }
      return getSerializationProperties(s);
    } catch (SaxonApiException e) {
      String msg = terminateMessage != null ? terminateMessage
          : e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new TransformationException(msg);
    }
  }

  @Override
  protected Serializer newSerializer(OutputStream os) {
    Serializer s = transformer.newSerializer(os);
    SerializationProperties props = new SerializationProperties();
    getSerializationParameters().forEach(props::setProperty);
    s.setOutputProperties(s.getSerializationProperties().combineWith(props));
    return s;
  }

  private Xslt30Transformer newTransformer(Source stylesheet) throws TransformationException {
    Processor p = getProcessor();
    XsltCompiler c = p.newXsltCompiler();
    logger.warn("Protocol restrictor test for https://www.google.com: {}",
    p.getUnderlyingConfiguration().getProtocolRestrictor().test(URI.create("https://www.google.com"))
);
    c.setErrorList(this.getErrorList());
    try {
      XsltExecutable e = c.compile(stylesheet);
      Xslt30Transformer xf = e.load30();
      xf.setResourceResolver(this.getResourceResolver());
      xf.setUnparsedTextResolver(this.getResourceResolver());
      xf.setMessageHandler(newMessageHandler());
      return xf;
    } catch (SaxonApiException e) {
      if (!this.getErrorList().isEmpty()) {
        XmlProcessingError error = this.getErrorList().get(0);
        Location location = error.getLocation();
        String message = error.getMessage() + " (line " + location.getLineNumber() + ", col "
            + location.getColumnNumber() + ")";
        throw new TransformationException("Compilation error: " + message);
      }
      throw new TransformationException(e);
    }
  }

  private Consumer<Message> newMessageHandler() {
    return msg -> {
      if (msg.isTerminate()) {
        terminateMessage = msg.getContent().getStringValue();
      } else {
        logger.info("Message received: \"{}\". Line: {}, column: {}", msg.getStringValue(),
            msg.getLocation().getLineNumber(), msg.getLocation().getColumnNumber());
      }
    };
  }
}
