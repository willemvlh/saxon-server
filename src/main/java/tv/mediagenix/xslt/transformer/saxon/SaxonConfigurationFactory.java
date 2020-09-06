package tv.mediagenix.xslt.transformer.saxon;

import net.sf.saxon.Configuration;

import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;

public abstract class SaxonConfigurationFactory {

    protected SaxonConfigurationFactory() {
    }

    public abstract Configuration newConfiguration();

    public abstract SAXSource newSAXSource(InputStream stream);
}

