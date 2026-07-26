package tv.mediagenix.xslt.transformer.saxon.actors;

import tv.mediagenix.xslt.transformer.saxon.core.SaxonResourceResolver;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.*;
import net.sf.saxon.serialize.SerializationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.mediagenix.xslt.transformer.saxon.core.JsonToXmlTransformer;
import tv.mediagenix.xslt.transformer.saxon.config.SaxonConfigurationFactory;
import tv.mediagenix.xslt.transformer.saxon.config.SaxonDefaultConfigurationFactory;
import tv.mediagenix.xslt.transformer.saxon.config.SaxonFixedConfigurationFactory;
import tv.mediagenix.xslt.transformer.saxon.config.SaxonSecureConfigurationFactory;
import tv.mediagenix.xslt.transformer.saxon.config.sax.DefaultSAXSourceFactory;
import tv.mediagenix.xslt.transformer.saxon.config.sax.SAXSourceFactory;
import tv.mediagenix.xslt.transformer.saxon.config.sax.SecureSAXSourceFactory;
import tv.mediagenix.xslt.transformer.saxon.core.SerializationProps;
import tv.mediagenix.xslt.transformer.saxon.core.TransformationException;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public abstract class SaxonActor {

    protected SaxonConfigurationFactory configurationFactory = new SaxonSecureConfigurationFactory();
    protected SAXSourceFactory saxSourceFactory = new SecureSAXSourceFactory();
    private Processor processor = null;
    private Map<String, String> serializationParameters = new HashMap<>();
    private Map<QName, XdmValue> parameters = new HashMap<>();
    private long timeout = 10000;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private URI baseURI = null;
    private SaxonResourceResolver resourceResolver = new SaxonResourceResolver(newConfiguration());;

    public SaxonResourceResolver getResourceResolver() {
      return resourceResolver;
    }

    public void setResourceResolver(SaxonResourceResolver resourceResolver) {
      this.resourceResolver = resourceResolver;
    }

    protected SaxonActor() {
    }

    private Configuration newConfiguration(){
        return this.configurationFactory.newConfiguration();
    }

    public void setConfiguration(Configuration config){
       this.configurationFactory = new SaxonFixedConfigurationFactory(config);
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
                logger.debug("Detected JSON input");
                JsonToXmlTransformer xf = new JsonToXmlTransformer();
                context = xf.transform(inputStreamToString(input), getProcessor());
            } else {
                DocumentBuilder b = getProcessor().newDocumentBuilder();
                context = b.build(saxSourceFactory.newSAXSource(input));
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
            logger.debug("Timeout limit ({} ms)was reached", timeout);
            throw new TransformationException(e);
        } catch (ExecutionException e) {
            throw new TransformationException((e.getCause() == null ? e : e.getCause()).getMessage(), e);
        } finally {
            service.shutdown();
        }
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
        if(processor == null){
            processor = new Processor(newConfiguration());
        }
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

    public void setInsecure(boolean insecure) {
        if (insecure) {
            this.configurationFactory = new SaxonDefaultConfigurationFactory();
            this.saxSourceFactory = new DefaultSAXSourceFactory();
            this.resourceResolver.setAllowExternalResources(true);
        } else {
            this.configurationFactory = new SaxonSecureConfigurationFactory();
            this.saxSourceFactory = new SecureSAXSourceFactory();
            this.resourceResolver.setAllowExternalResources(false);
        }
        this.setProcessor(new Processor(newConfiguration()));
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

    public URI getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(URI baseURI) {
        this.baseURI = baseURI;
        if(baseURI != null) {
          this.resourceResolver.setBaseURI(baseURI);
        }
    }
}
