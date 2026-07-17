package tv.mediagenix.xslt.transformer.saxon.config;

import net.sf.saxon.Configuration;

public class SaxonDefaultConfigurationFactory extends SaxonConfigurationFactory {

    @Override
    public Configuration newConfiguration() {
        return new Configuration();
    }
}
