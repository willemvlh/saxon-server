package tv.mediagenix.xslt.transformer.saxon.config;

import net.sf.saxon.Configuration;
import tv.mediagenix.xslt.transformer.saxon.core.SaxonResourceResolver;

public class SaxonDefaultConfigurationFactory extends SaxonConfigurationFactory {

  @Override
  public Configuration newConfiguration() {
    var config = new Configuration();
    var resolver = new SaxonResourceResolver(true, config.getResourceResolver(),
        config.getUnparsedTextURIResolver(), config.getProtocolRestrictor());
    resolver.setBaseURI(this.getBaseURI());
    resolver.setFiles(this.getFiles());
    config.setResourceResolver(resolver);
    return config;
  }
}
