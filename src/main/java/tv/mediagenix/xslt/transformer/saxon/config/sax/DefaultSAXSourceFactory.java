package tv.mediagenix.xslt.transformer.saxon.config.sax;

import org.xml.sax.InputSource;

import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;

public class DefaultSAXSourceFactory extends SAXSourceFactory {
    @Override
    public SAXSource newSAXSource(InputStream stream) {
        return new SAXSource(new InputSource(stream));
    }
}
