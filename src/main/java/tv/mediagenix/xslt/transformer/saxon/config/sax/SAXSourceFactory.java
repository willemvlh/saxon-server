package tv.mediagenix.xslt.transformer.saxon.config.sax;

import tv.mediagenix.xslt.transformer.saxon.TransformationException;

import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;

public abstract class SAXSourceFactory {
    public abstract SAXSource newSAXSource(InputStream stream) throws TransformationException;
}
