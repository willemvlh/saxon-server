package tv.mediagenix.xslt.transformer.saxon.config.sax;

import org.xml.sax.InputSource;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;

public class SecureSAXSourceFactory extends SAXSourceFactory {
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
