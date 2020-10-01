package tv.mediagenix.xslt.transformer.saxon.actors;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.*;
import net.sf.saxon.serialize.SerializationProperties;
import tv.mediagenix.xslt.transformer.saxon.JsonToXmlTransformer;
import tv.mediagenix.xslt.transformer.saxon.SerializationProps;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;
import tv.mediagenix.xslt.transformer.saxon.config.SaxonConfigurationFactory;
import tv.mediagenix.xslt.transformer.saxon.config.SaxonDefaultConfigurationFactory;
import tv.mediagenix.xslt.transformer.saxon.config.SaxonSecureConfigurationFactory;

import javax.xml.transform.sax.SAXSource;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public abstract class SaxonActor {

    protected SaxonConfigurationFactory configurationFactory = new SaxonSecureConfigurationFactory();
    private Processor processor = new Processor(this.configurationFactory.newConfiguration());
    private Map<String, String> serializationParameters = new HashMap<>();
    private Configuration configuration;
    private boolean insecure = false;

    SaxonActor() {
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

    protected SerializationProps getSerializationProperties(Serializer s) {
        return new SerializationProps(s.getOutputProperty(Serializer.Property.MEDIA_TYPE), s.getOutputProperty(Serializer.Property.ENCODING));
    }

    protected Map<String, String> getSerializationParameters() {
        return serializationParameters;
    }

    public void setSerializationParameters(Map<String, String> serializationParameters) {
        this.serializationParameters = serializationParameters;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        this.setProcessor(new Processor(configuration));
    }

    public Configuration getConfiguration() {
        if (configuration == null) {
            configuration = configurationFactory.newConfiguration();
        }
        return configuration;
    }

    public void setInsecure(boolean insecure) {
        this.insecure = insecure;
        if (insecure) {
            this.configurationFactory = new SaxonDefaultConfigurationFactory();
            this.setProcessor(new Processor(this.getConfiguration()));
        }
    }


    public void setProcessor(Processor processor) {
        this.processor = processor;
    }
}
