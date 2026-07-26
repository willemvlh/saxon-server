package tv.mediagenix.xslt.transformer.saxon.actors;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import tv.mediagenix.xslt.transformer.saxon.core.TransformationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public abstract class SaxonActorBuilder {
  private final SaxonActor instance = this.createNewInstance();

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  protected abstract SaxonActor createNewInstance();

  public SaxonActorBuilder setSerializationProperties(Map<String, String> parameters) {
    instance.setSerializationParameters(parameters);
    return this;
  }

  public SaxonActorBuilder setTimeout(long milliseconds) {
    if (milliseconds > 0) {
      instance.setTimeout(milliseconds);
    }
    return this;
  }

  public SaxonActorBuilder setConfigurationFile(File file) throws TransformationException {
    if (file == null)
      return this;
    Configuration config;
    try {
      config = Configuration.readConfiguration(new StreamSource(file));
    } catch (XPathException e) {
      throw new TransformationException(e);
    }
    instance.setConfiguration(config);
    return this;
  }

  public SaxonActorBuilder setInsecure(boolean insecure) {
    instance.setInsecure(insecure);
    return this;
  }

  public SaxonActor build() {
    return instance;
  }

  public SaxonActorBuilder setBaseURI(URI baseURI) {
    instance.setBaseURI(baseURI);
    return this;
  }

  public SaxonActorBuilder setParameters(Map<String, String> parameters) {
    Map<QName, XdmValue> qNameParams = new HashMap<>();
    parameters.forEach((k, v) -> {
      qNameParams.put(new QName(k), XdmAtomicValue.makeAtomicValue(v));
    });
    instance.setParameters(qNameParams);
    return this;
  }

  public SaxonActorBuilder setFiles(Map<String, InputStream> files) {
    instance.getResourceResolver().setFiles(files);
    return this;
  }

}
