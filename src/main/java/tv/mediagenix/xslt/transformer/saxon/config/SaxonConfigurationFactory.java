package tv.mediagenix.xslt.transformer.saxon.config;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import net.sf.saxon.Configuration;
import tv.mediagenix.xslt.transformer.saxon.core.SaxonResourceResolver;

public abstract class SaxonConfigurationFactory {

  protected SaxonConfigurationFactory() {
  }

  private Map<String, InputStream> files = new HashMap<>();

  public Map<String, InputStream> getFiles() {
    return files;
  }

  public void setFiles(Map<String, InputStream> files) {
    this.files = files;
  }

  private URI baseURI = Paths.get("").toAbsolutePath().toUri();

  public URI getBaseURI() {
    return baseURI;
  }

  public void setBaseURI(URI baseUri) {
    this.baseURI = baseUri;
  }

  protected abstract boolean allowExternalResources();

  public Configuration newConfiguration() {

    var config = new Configuration();
    var resolver = newResourceResolver(config);
    config.setResourceResolver(resolver);
    config.setUnparsedTextURIResolver(resolver);
    return config;
  }

  protected SaxonResourceResolver newResourceResolver(Configuration config){

    var resolver = new SaxonResourceResolver(allowExternalResources(), config.getResourceResolver(),
        config.getUnparsedTextURIResolver(), config.getProtocolRestrictor());
    resolver.setBaseURI(this.getBaseURI());
    resolver.setFiles(this.getFiles());
    return resolver;
  }

}
