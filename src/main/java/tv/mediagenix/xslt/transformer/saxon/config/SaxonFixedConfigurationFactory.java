package tv.mediagenix.xslt.transformer.saxon.config;

import net.sf.saxon.Configuration;

public class SaxonFixedConfigurationFactory extends SaxonConfigurationFactory {

  private final Configuration configuration;

  public SaxonFixedConfigurationFactory(Configuration configuration) {
    this.configuration = configuration;
    var resolver = newResourceResolver(this.configuration);
    this.configuration.setResourceResolver(resolver);
    this.configuration.setUnparsedTextURIResolver(resolver);
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
