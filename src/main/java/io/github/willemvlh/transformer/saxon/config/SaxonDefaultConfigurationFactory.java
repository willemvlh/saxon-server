package io.github.willemvlh.transformer.saxon.config;

import com.saxonica.config.EnterpriseConfiguration;
import net.sf.saxon.Configuration;
import org.xml.sax.InputSource;

import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;

public class SaxonDefaultConfigurationFactory extends SaxonConfigurationFactory {

    @Override
    public Configuration newConfiguration() {
        return new EnterpriseConfiguration();
    }

    @Override
    public SAXSource newSAXSource(InputStream stream) {
        return new SAXSource(new InputSource(stream));
    }

}
