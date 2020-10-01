package tv.mediagenix.xslt.transformer.saxon.actors;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.*;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import tv.mediagenix.xslt.transformer.saxon.JsonToXmlTransformer;
import tv.mediagenix.xslt.transformer.saxon.SerializationProps;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;
import tv.mediagenix.xslt.transformer.saxon.config.SaxonConfigurationFactory;
import tv.mediagenix.xslt.transformer.saxon.config.SaxonDefaultConfigurationFactory;
import tv.mediagenix.xslt.transformer.saxon.config.SaxonSecureConfigurationFactory;

import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public abstract class SaxonActor {

    protected SaxonConfigurationFactory configurationFactory = new SaxonSecureConfigurationFactory();
    private Processor processor;
    private Map<String, String> serializationParameters = new HashMap<>();

    public SaxonActor(boolean insecure) {
        this();
        this.configurationFactory = insecure ? new SaxonDefaultConfigurationFactory() : new SaxonSecureConfigurationFactory();
    }

    public SaxonActor() {
        this.setProcessor(newProcessorWithDefaults());
    }

    public SaxonActor(@NotNull File configFile) throws TransformationException {
        try {
            Configuration config = Configuration.readConfiguration(new StreamSource(configFile));
            setProcessor(newProcessorWithConfig(config));
        } catch (XPathException e) {
            LoggerFactory.getLogger(this.getClass()).error(e.getMessage());
            throw new TransformationException(e);
        }
    }

    public final SerializationProps act(InputStream input, InputStream input2, OutputStream output) throws TransformationException {
        try {
            if (isJsonStream(input)) {
                JsonToXmlTransformer xf = new JsonToXmlTransformer();
                XdmNode jsonAsXml = xf.transform(inputStreamToString(input), getProcessor());
                return act(null, input2, output, jsonAsXml);
            }
            return act(input, input2, output, null);
        } catch (SaxonApiException e) {
            throw new TransformationException(e);
        }
    }

    private String inputStreamToString(InputStream input) throws TransformationException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        try {
            int c;
            while ((c = reader.read()) != -1) {
                builder.append((char) c);
            }
            return builder.toString();
        } catch (IOException e) {
            throw new TransformationException(e);
        }

    }

    //perhaps change the interface to send the input always as an XdmItem
    public abstract SerializationProps act(InputStream input, InputStream input2, OutputStream output, XdmItem contextNode) throws TransformationException;

    public abstract SerializationProps act(InputStream input, OutputStream output) throws TransformationException;

    protected SAXSource newSAXSource(InputStream stream) {
        return this.configurationFactory.newSAXSource(stream);
    }

    private boolean isJsonStream(InputStream stream) throws TransformationException {
        char c;
        try {
            c = (char) stream.read();
            if (c == '\uFFFF') {
                //eof
                return false;
            }
            while (Character.isWhitespace(c)) {
                c = (char) stream.read();
            }
            stream.reset();
            return c != '<';
        } catch (IOException e) {
            throw new TransformationException(e);
        }
    }

    private Processor newProcessorWithConfig(Configuration config) {
        return new Processor(config);
    }

    private Processor newProcessorWithDefaults() {
        return new Processor(this.configurationFactory.newConfiguration());
    }

    protected Serializer newSerializer(OutputStream os) {
        Serializer serializer = this.getProcessor().newSerializer(os);
        SerializationProperties props = new SerializationProperties();
        for (String s : this.getSerializationParameters().keySet()) {
            props.setProperty(s, this.getSerializationParameters().get(s));
        }
        serializer.setOutputProperties(props);
        return serializer;
    }

    Processor getProcessor() {
        return processor;
    }

    private void setProcessor(Processor processor) {
        this.processor = processor;
    }

    protected SerializationProps getSerializationProperties(Serializer s) {
        return new SerializationProps(s.getOutputProperty(Serializer.Property.MEDIA_TYPE), s.getOutputProperty(Serializer.Property.ENCODING));
    }

    protected Map<String, String> getSerializationParameters() {
        return serializationParameters;
    }

    public void setSerializationParameters(Map<String, String> serializationParameters) {
        this.serializationParameters = serializationParameters;
    }
}
