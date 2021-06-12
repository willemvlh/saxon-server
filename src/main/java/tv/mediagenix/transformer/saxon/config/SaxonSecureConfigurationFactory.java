package tv.mediagenix.transformer.saxon.config;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Feature;
import org.xml.sax.InputSource;

import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;

public class SaxonSecureConfigurationFactory extends SaxonConfigurationFactory {

    @Override
    public Configuration newConfiguration() {
        Configuration config = new Configuration();
        config.setAllowedUriTest(uri -> false);
        config.setConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS, false);
        return config;
    }

    @Override
    public SAXSource newSAXSource(InputStream stream) {
        SAXParser p = new SAXParser();
        try {
            p.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SAXSource(p, new InputSource(stream));
    }
}
