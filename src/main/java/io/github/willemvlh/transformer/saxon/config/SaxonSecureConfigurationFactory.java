package io.github.willemvlh.transformer.saxon.config;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Feature;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;

public class SaxonSecureConfigurationFactory extends SaxonConfigurationFactory {

    @Override
    public Configuration newConfiguration() {
        Configuration config = new Configuration();
        config.setConfigurationProperty(Feature.ALLOWED_PROTOCOLS, "");
        config.setConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS, false);
        return config;
    }

    @Override
    public SAXSource newSAXSource(InputStream stream) throws ParserConfigurationException, SAXException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        var parser = spf.newSAXParser();
        try {
            //parser.setProperty(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SAXSource(parser.getXMLReader(), new InputSource(stream));
    }
}
