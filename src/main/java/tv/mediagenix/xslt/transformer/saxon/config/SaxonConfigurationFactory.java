package tv.mediagenix.xslt.transformer.saxon.config;

import net.sf.saxon.Configuration;

public abstract class SaxonConfigurationFactory {

    protected SaxonConfigurationFactory() {
    }

    public abstract Configuration newConfiguration();
}

