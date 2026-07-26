package tv.mediagenix.xslt.transformer.saxon.config.sax;

import javax.xml.transform.sax.SAXSource;

import tv.mediagenix.xslt.transformer.saxon.core.TransformationException;

import java.io.InputStream;

public abstract class SAXSourceFactory {
    public abstract SAXSource newSAXSource(InputStream stream) throws TransformationException;
}
