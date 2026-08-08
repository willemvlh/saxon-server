package tv.mediagenix.xslt.transformer.saxon.config;

import org.slf4j.LoggerFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.trans.XPathException;
import tv.mediagenix.xslt.transformer.saxon.core.SaxonResourceResolver;

public class SaxonSecureConfigurationFactory extends SaxonConfigurationFactory {
  public Configuration newConfiguration() {
    Configuration config = super.newConfiguration();
    config.setCollectionFinder((context, collectionURI) -> {
      throw new XPathException("Collection access is not allowed.");
    });
    /*
     * The 'file' protocol is allowed to enable access to attached files
     */
    config.setConfigurationProperty(Feature.ALLOWED_PROTOCOLS, "file");
    config.setConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS, false);
    return config;
  }

  @Override
  protected boolean allowExternalResources() {
    return false;
  }
}
