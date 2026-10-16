package tv.mediagenix.xslt.transformer.saxon.config;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.StandardUnparsedTextResolver;

public class SaxonFixedConfigurationFactory extends SaxonConfigurationFactory {

  private final Configuration configuration;

  public SaxonFixedConfigurationFactory(Configuration configuration) {
    this.configuration = configuration;
    var resolver = newResourceResolver(this.configuration);
    this.configuration.setResourceResolver(resolver);
    this.configuration.setUnparsedTextURIResolver(new StandardUnparsedTextResolver());
  }

  @Override
  public Configuration newConfiguration() {
    return configuration;
  }

  @Override
  protected boolean allowExternalResources() {
    return true;
  }
}
