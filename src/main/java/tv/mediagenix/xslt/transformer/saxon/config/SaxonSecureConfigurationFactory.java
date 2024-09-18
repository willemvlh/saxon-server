package tv.mediagenix.xslt.transformer.saxon.config;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Feature;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;

public class SaxonSecureConfigurationFactory extends SaxonConfigurationFactory {
    public Configuration newConfiguration() {
        Configuration config = new Configuration();
        config.setConfigurationProperty(Feature.ALLOWED_PROTOCOLS, "");
        config.setConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS, false);
        return config;
    }

    @Override
    public SAXSource newSAXSource(InputStream stream) throws TransformationException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser parser = spf.newSAXParser();
            parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return new SAXSource(parser.getXMLReader(), new InputSource(stream));
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

}
