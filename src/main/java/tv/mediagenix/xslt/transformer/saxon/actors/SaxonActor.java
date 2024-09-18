package tv.mediagenix.xslt.transformer.saxon.actors;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.*;
import net.sf.saxon.serialize.SerializationProperties;
import org.xml.sax.SAXException;
import tv.mediagenix.xslt.transformer.saxon.JsonToXmlTransformer;
import tv.mediagenix.xslt.transformer.saxon.SerializationProps;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;
import tv.mediagenix.xslt.transformer.saxon.config.SaxonConfigurationFactory;
import tv.mediagenix.xslt.transformer.saxon.config.SaxonDefaultConfigurationFactory;
import tv.mediagenix.xslt.transformer.saxon.config.SaxonSecureConfigurationFactory;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public abstract class SaxonActor {

    private SaxonConfigurationFactory configurationFactory = new SaxonSecureConfigurationFactory();
    private Processor processor = new Processor(this.configurationFactory.newConfiguration());
    private Map<String, String> serializationParameters = new HashMap<>();
    private Map<QName, XdmValue> parameters = new HashMap<>();
    private Configuration configuration;
    private long timeout = 10000;

    protected SaxonActor() {
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

    public SerializationProps act(InputStream input, InputStream stylesheet, OutputStream output) throws TransformationException {
        XdmItem context;
        try {
            if (isJsonStream(input)) {
                JsonToXmlTransformer xf = new JsonToXmlTransformer();
                context = xf.transform(inputStreamToString(input), getProcessor());
            } else {
                DocumentBuilder b = getProcessor().newDocumentBuilder();
                context = b.build(newSAXSource(input));
            }
            return actWithTimeout(context, stylesheet, output);
        } catch (SaxonApiException e) {
            throw new TransformationException(e);
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
            throw new TransformationException(e);
        } catch (ExecutionException e) {
            throw new TransformationException((e.getCause() == null ? e : e.getCause()).getMessage(), e);
        } finally {
            service.shutdown();
        }
    }

    protected SAXSource newSAXSource(InputStream stream) throws TransformationException{
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

    public void setInsecure(boolean insecure) {
        if (insecure) {
            this.configurationFactory = new SaxonDefaultConfigurationFactory();
        } else {
            this.configurationFactory = new SaxonSecureConfigurationFactory();
        }
        this.setProcessor(new Processor(this.configurationFactory.newConfiguration()));
    }


    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

    public void setTimeout(long milliseconds) {
        this.timeout = milliseconds;
    }

    public void setParameters(Map<QName, XdmValue> parameters) {
        this.parameters = parameters;
    }
}
