package tv.mediagenix.xslt.transformer.saxon.config;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Feature;

public class SaxonSecureConfigurationFactory extends SaxonConfigurationFactory {
    public Configuration newConfiguration() {
        Configuration config = new Configuration();
        //config.setConfigurationProperty(Feature.ALLOWED_PROTOCOLS, "");
        config.setConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS, false);
        return config;
    }
}
