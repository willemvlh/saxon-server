package io.github.willemvlh.transformer.saxon.actors;

import io.github.willemvlh.transformer.saxon.Convert;
import io.github.willemvlh.transformer.saxon.JsonToXmlTransformer;
import io.github.willemvlh.transformer.saxon.SerializationProps;
import io.github.willemvlh.transformer.saxon.TransformationException;
import io.github.willemvlh.transformer.saxon.config.SaxonConfigurationFactory;
import io.github.willemvlh.transformer.saxon.config.SaxonDefaultConfigurationFactory;
import io.github.willemvlh.transformer.saxon.config.SaxonSecureConfigurationFactory;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.*;
import net.sf.saxon.serialize.SerializationProperties;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public abstract class SaxonActor {

    private SaxonConfigurationFactory configurationFactory = new SaxonSecureConfigurationFactory();
    private Configuration configuration;
    private Processor processor;
    private Map<String, String> serializationParameters = new HashMap<>();
    private Map<QName, XdmValue> parameters = new HashMap<>();
    private long timeout = 10000;

    protected SaxonActor() {
    }

    public SerializationProps act(InputStream input, InputStream stylesheet, OutputStream output) throws TransformationException {
        XdmItem context;
        try {
            if (isJsonStream(input)) {
                JsonToXmlTransformer xf = new JsonToXmlTransformer();
                context = xf.transform(Convert.toString(input), getProcessor());
            } else {
                DocumentBuilder b = getProcessor().newDocumentBuilder();
                context = b.build(newSAXSource(input));
            }
            return actWithTimeout(context, stylesheet, output);
        } catch (SaxonApiException | IOException e) {
            throw new TransformationException(e.getMessage());
        }
    }

    public SerializationProps act(InputStream stylesheet, OutputStream os) throws TransformationException {
        return actWithTimeout(XdmEmptySequence.getInstance(), stylesheet, os);
    }

    protected abstract SerializationProps act(XdmValue input, InputStream stylesheet, OutputStream output) throws TransformationException;

    protected SerializationProps actWithTimeout(XdmValue input, InputStream stylesheet, OutputStream output) throws TransformationException {
        ExecutorService service = new ForkJoinPool();
        try {
            FutureTask<SerializationProps> task = new FutureTask<>(() -> act(input, stylesheet, output));
            service.submit(task);
            return task.get(this.timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException | InterruptedException e) {
            throw new TransformationException(String.format("Timeout exceeded (%s ms)", timeout), e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TransformationException) {
                throw (TransformationException) e.getCause();
            }
            throw new TransformationException((e.getCause() == null ? e : e.getCause()).getMessage(), e);
        } finally {
            service.shutdown();
        }
    }

    protected SAXSource newSAXSource(InputStream stream) {
        return this.configurationFactory.newSAXSource(stream);
    }

    private boolean isJsonStream(InputStream stream) throws TransformationException {
        stream.mark(10);
        int readCount = 0;
        char c;
        try {
            c = (char) stream.read();
            if (c == '\uFFFF') {
                //eof
                return false;
            }
            while (Character.isWhitespace(c) && readCount < 10) {
                readCount++;
                c = (char) stream.read();
            }
            stream.reset();
            return c != '<';
        } catch (IOException e) {
            throw new TransformationException(e.getMessage());
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

    protected Map<QName, XdmValue> getParameters() {
        return parameters;
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

    public void setInsecure() {
        this.configurationFactory = new SaxonDefaultConfigurationFactory();
        this.setConfiguration(configurationFactory.newConfiguration());
    }

    public void setSecure() {
        this.configurationFactory = new SaxonSecureConfigurationFactory();
        this.setConfiguration(configurationFactory.newConfiguration());
    }

    public void setTimeout(long milliseconds) {
        if (milliseconds == -1) {
            this.timeout = 86400000;
        } else if (milliseconds < 1) {
            throw new IllegalArgumentException("This argument requires a positive number or -1 for no timeout");
        } else {
            this.timeout = milliseconds;
        }

    }

    public long getTimeout() {
        return this.timeout;
    }

    public void setParameters(Map<QName, XdmValue> parameters) {
        this.parameters = parameters;
    }

    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

}
