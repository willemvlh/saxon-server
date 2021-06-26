package tv.mediagenix.transformer.saxon.actors;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import tv.mediagenix.transformer.saxon.TransformationException;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class SaxonActorBuilder {

    private long timeOut = 10000;
    private Map<String, String> serializationParameters = new HashMap<>();
    private Map<QName, XdmValue> parameters = new HashMap<>();
    private Configuration configuration;
    private String licenseFile;
    private boolean isInsecure = false;

    public abstract Class<? extends SaxonActor> getActorClass();

    public SaxonActorBuilder setSerializationProperties(Map<String, String> parameters) {
        this.serializationParameters = parameters;
        return this;
    }

    public SaxonActorBuilder setTimeout(long milliseconds) {
        this.timeOut = milliseconds;
        return this;
    }

    public SaxonActorBuilder setConfigurationFile(File file) throws TransformationException {
        if (file == null) return this;
        Configuration config;
        try {
            config = Configuration.readConfiguration(new StreamSource(file));
        } catch (XPathException e) {
            throw new TransformationException(e);
        }
        this.configuration = config;
        return this;
    }

    public SaxonActorBuilder setInsecure(boolean insecure) {
        this.isInsecure = insecure;
        return this;
    }

    public SaxonActorBuilder setLicenseFile(String file) {
        this.licenseFile = file;
        return this;
    }

    public SaxonActor build() {
        try {
            SaxonActor instance = this.getActorClass().newInstance();
            instance.setConfiguration(configuration);
            if (isInsecure) instance.setInsecure();
            instance.setTimeout(timeOut);
            instance.setParameters(parameters);
            instance.setSerializationParameters(serializationParameters);
            if (this.licenseFile != null) {
                instance.getConfiguration().setConfigurationProperty(Feature.LICENSE_FILE_LOCATION, this.licenseFile);
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public SaxonActorBuilder setParameters(Map<String, String> parameters) {
        Map<QName, XdmValue> qNameParams = new HashMap<>();
        parameters.forEach((k, v) -> {
            qNameParams.put(new QName(k), XdmAtomicValue.makeAtomicValue(v));
        });
        this.parameters = qNameParams;
        return this;
    }
}

