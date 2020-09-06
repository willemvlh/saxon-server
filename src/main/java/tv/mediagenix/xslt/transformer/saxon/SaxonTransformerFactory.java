package tv.mediagenix.xslt.transformer.saxon;

import tv.mediagenix.xslt.transformer.saxon.actors.SaxonActor;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonTransformer;

import java.io.File;

public class SaxonTransformerFactory extends AbstractSaxonActorFactory {

    @Override
    public SaxonActor newActor(boolean insecure) throws TransformationException {
        return new SaxonTransformer(insecure);
    }

    @Override
    public SaxonActor newActorWithConfig(File config) throws TransformationException {
        return new SaxonTransformer(config);
    }
}
