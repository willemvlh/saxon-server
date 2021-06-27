package tv.mediagenix.transformer.saxon.actors;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import tv.mediagenix.transformer.saxon.config.SaxonConfigurationFactory;
import tv.mediagenix.transformer.saxon.config.SaxonSecureConfigurationFactory;

import java.util.HashMap;
import java.util.Map;

public abstract class SaxonActorBuilder {

    private long timeOut = 10000;
    private Map<String, String> serializationParameters = new HashMap<>();
    private Map<QName, XdmValue> parameters = new HashMap<>();
    private Processor processor;
    private SaxonConfigurationFactory configurationFactory = new SaxonSecureConfigurationFactory();

    public abstract Class<? extends SaxonActor> getActorClass();

    public SaxonActorBuilder setSerializationProperties(Map<String, String> parameters) {
        this.serializationParameters = parameters;
        return this;
    }

    public SaxonActorBuilder setTimeout(long milliseconds) {
        this.timeOut = milliseconds;
        return this;
    }

    public SaxonActor build() {
        try {
            SaxonActor instance = this.getActorClass().newInstance();
            instance.setProcessor(getProcessor());
            instance.setTimeout(timeOut);
            instance.setParameters(parameters);
            instance.setSerializationParameters(serializationParameters);
            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Processor getProcessor() {
        if (processor == null) {
            processor = new Processor(configurationFactory.newConfiguration());
        }
        return processor;
    }

    public SaxonActorBuilder setParameters(Map<String, String> parameters) {
        Map<QName, XdmValue> qNameParams = new HashMap<>();
        parameters.forEach((k, v) -> {
            qNameParams.put(new QName(k), XdmAtomicValue.makeAtomicValue(v));
        });
        this.parameters = qNameParams;
        return this;
    }

    public SaxonActorBuilder setProcessor(Processor processor) {
        this.processor = processor;
        return this;
    }
}

