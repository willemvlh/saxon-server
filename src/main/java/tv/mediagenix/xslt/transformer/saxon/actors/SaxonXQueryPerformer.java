package tv.mediagenix.xslt.transformer.saxon.actors;

import net.sf.saxon.s9api.*;
import net.sf.saxon.serialize.SerializationProperties;
import tv.mediagenix.xslt.transformer.saxon.core.SerializationProps;
import tv.mediagenix.xslt.transformer.saxon.core.TransformationException;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

public class SaxonXQueryPerformer extends SaxonActor {
  private XQueryExecutable executable;

  private XQueryEvaluator newEvaluator(InputStream query) throws SaxonApiException {
    var compiler = this.getProcessor().newXQueryCompiler();
    var baseUri = getBaseURI() == null ? Paths.get("").toAbsolutePath().toUri().toString() : getBaseURI();
    compiler.getUnderlyingStaticContext().setBaseURI(baseUri);
    this.executable = compiler.compile(query);
    XQueryEvaluator evaluator = this.executable.load();
    evaluator.setResourceResolver(this.getResourceResolver());
    this.getParameters().forEach(evaluator::setExternalVariable);
    return evaluator;
  }

  @Override
  public SerializationProps act(XdmValue input, InputStream query, OutputStream output) throws TransformationException {
    try {
      XQueryEvaluator e = newEvaluator(query);
      if (!input.isEmpty()) {
        e.setContextItem(input.itemAt(0));
      }
      return evaluate(e, output);
    } catch (SaxonApiException e) {
      throw new TransformationException(e.getMessage(), e);
    }
  }

  @Override
  protected SerializationProps getSerializationProperties(Serializer s) {
    SerializationProperties props = this.executable.getUnderlyingCompiledQuery().getExecutable()
        .getPrimarySerializationProperties();
    props = props.combineWith(s.getSerializationProperties());
    return new SerializationProps(props.getProperty("media-type"), props.getProperty("encoding"));
  }

  private SerializationProps evaluate(XQueryEvaluator e, OutputStream output) throws SaxonApiException {
    Serializer s = newSerializer(output);
    e.setDestination(s);
    e.run();
    return getSerializationProperties(s);
  }

}
