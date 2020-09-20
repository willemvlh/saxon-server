package tv.mediagenix.xslt.transformer.saxon.actors;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.trans.XPathException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import tv.mediagenix.xslt.transformer.saxon.SerializationProps;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;
import tv.mediagenix.xslt.transformer.saxon.config.SaxonConfigurationFactory;
import tv.mediagenix.xslt.transformer.saxon.config.SaxonDefaultConfigurationFactory;
import tv.mediagenix.xslt.transformer.saxon.config.SaxonSecureConfigurationFactory;

import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class SaxonActor {

    protected SaxonConfigurationFactory configurationFactory = new SaxonSecureConfigurationFactory();
    private Processor processor;

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

    public abstract SerializationProps act(InputStream input, InputStream input2, OutputStream output) throws TransformationException;

    public abstract SerializationProps act(InputStream input, OutputStream output) throws TransformationException;

    protected SAXSource newSAXSource(InputStream stream) {
        return this.configurationFactory.newSAXSource(stream);
    }

    private Processor newProcessorWithConfig(Configuration config) {
        return new Processor(config);
    }

    private Processor newProcessorWithDefaults() {
        return new Processor(this.configurationFactory.newConfiguration());
    }

    protected Serializer newSerializer(OutputStream os) {
        return this.getProcessor().newSerializer(os);
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
}
