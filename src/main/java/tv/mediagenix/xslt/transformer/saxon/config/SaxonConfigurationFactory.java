package tv.mediagenix.xslt.transformer.saxon.config;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.StandardUnparsedTextResolver;
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
    configure(config);
    var resolver = newResourceResolver(config);
    config.setResourceResolver(resolver);
    config.setUnparsedTextURIResolver(new StandardUnparsedTextResolver());
    return config;
  }

  /*
   * Called before the resource resolver is installed. Settings such as
   * Feature.ALLOWED_PROTOCOLS wrap the current resource resolver, so they must be
   * applied here to keep SaxonResourceResolver as the outermost resolver.
   */
  protected void configure(Configuration config) {
  }

  protected SaxonResourceResolver newResourceResolver(Configuration config){

    var resolver = new SaxonResourceResolver(allowExternalResources(), config.getResourceResolver(),
        config.getProtocolRestrictor());
    resolver.setBaseURI(this.getBaseURI());
    resolver.setFiles(this.getFiles());
    return resolver;
  }

}
