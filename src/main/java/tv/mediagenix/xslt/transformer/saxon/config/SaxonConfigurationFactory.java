package tv.mediagenix.xslt.transformer.saxon.config;

import net.sf.saxon.Configuration;
import org.xml.sax.SAXException;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;

public abstract class SaxonConfigurationFactory {

    protected SaxonConfigurationFactory() {
    }

    public abstract Configuration newConfiguration();

    public abstract SAXSource newSAXSource(InputStream stream) throws TransformationException;
}

