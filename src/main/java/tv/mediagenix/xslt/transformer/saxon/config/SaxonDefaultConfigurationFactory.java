package tv.mediagenix.xslt.transformer.saxon.config;

public class SaxonDefaultConfigurationFactory extends SaxonConfigurationFactory {

  @Override
  protected boolean allowExternalResources() {
    return true;
  }
}
