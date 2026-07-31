package tv.mediagenix.xslt.transformer.saxon.config;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.trans.XPathException;

public class SaxonSecureConfigurationFactory extends SaxonConfigurationFactory {
    public Configuration newConfiguration() {
        Configuration config = new Configuration();
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
}
