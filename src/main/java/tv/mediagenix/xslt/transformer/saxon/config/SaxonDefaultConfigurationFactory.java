package tv.mediagenix.xslt.transformer.saxon.config;

import org.slf4j.LoggerFactory;

import net.sf.saxon.Configuration;
import tv.mediagenix.xslt.transformer.saxon.core.SaxonResourceResolver;

public class SaxonDefaultConfigurationFactory extends SaxonConfigurationFactory {

  @Override
  protected boolean allowExternalResources() {
    return true;
  }
}
