package tv.mediagenix.transformer.app;

import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import tv.mediagenix.transformer.saxon.TransformationException;
import tv.mediagenix.transformer.saxon.actors.ActorType;
import tv.mediagenix.transformer.saxon.actors.SaxonActorBuilder;
import tv.mediagenix.transformer.saxon.actors.SaxonTransformerBuilder;
import tv.mediagenix.transformer.saxon.actors.SaxonXQueryPerformerBuilder;

@Component
public class TransformerConfiguration {
    private SaxonTransformerBuilder transformer;
    private SaxonXQueryPerformerBuilder xQueryPerformer;
    private ServerOptions options;

    @Autowired
    public TransformerConfiguration(ApplicationArguments args) throws ParseException {
        this.options = ServerOptions.fromArgs(args.getSourceArgs());
    }

    public SaxonTransformerBuilder getSaxonTransformerBuilder() throws TransformationException {
        if (transformer == null) {
            return transformer = (SaxonTransformerBuilder) build(ActorType.TRANSFORM);
        }
        return transformer;
    }

    public SaxonXQueryPerformerBuilder getSaxonXQueryPerformerBuilder() throws TransformationException {
        if (xQueryPerformer == null) {
            xQueryPerformer = (SaxonXQueryPerformerBuilder) build(ActorType.QUERY);
        }
        return xQueryPerformer;
    }

    private SaxonActorBuilder build(ActorType actorType) throws TransformationException {
        SaxonActorBuilder builder = actorType.getBuilder();
        return builder
                .setTimeout(options.getTransformationTimeoutMs())
                .setConfigurationFile(options.getConfigFile())
                .setInsecure(options.isInsecure())
                .setLicenseFile(options.getLicenseFilepath());
    }
}
