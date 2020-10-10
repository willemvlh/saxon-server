package tv.mediagenix.xslt.transformer.saxon.actors;

import net.sf.saxon.Configuration;
import net.sf.saxon.trans.XPathException;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.Map;

public abstract class SaxonActorBuilder {
    private final SaxonActor instance;

    public SaxonActorBuilder() {
        try {
            this.instance = this.getActorClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public abstract Class<? extends SaxonActor> getActorClass();

    public SaxonActorBuilder setSerializationProperties(Map<String, String> parameters) {
        instance.setSerializationParameters(parameters);
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
        instance.setConfiguration(config);
        return this;
    }

    public SaxonActorBuilder setInsecure(boolean insecure) {
        instance.setInsecure(insecure);
        return this;
    }

    public SaxonActor build() {
        return instance;
    }

}

