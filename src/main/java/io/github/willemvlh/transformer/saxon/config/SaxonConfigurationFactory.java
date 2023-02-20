package io.github.willemvlh.transformer.saxon.config;

import net.sf.saxon.Configuration;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;

public abstract class SaxonConfigurationFactory {

    protected SaxonConfigurationFactory() {
    }

    public abstract Configuration newConfiguration();

    public abstract SAXSource newSAXSource(InputStream stream) throws ParserConfigurationException, SAXException;
}

