package tv.mediagenix.xslt.transformer.saxon.config;

import net.sf.saxon.Configuration;

public class SaxonFixedConfigurationFactory extends SaxonConfigurationFactory {

    private final Configuration configuration;

    public SaxonFixedConfigurationFactory(Configuration configuration){
        this.configuration = configuration;
    }
    @Override
    public Configuration newConfiguration() {
        return configuration;
    }
}
