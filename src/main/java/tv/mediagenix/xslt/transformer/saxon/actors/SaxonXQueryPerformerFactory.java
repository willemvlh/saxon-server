package tv.mediagenix.xslt.transformer.saxon.actors;

import tv.mediagenix.xslt.transformer.saxon.TransformationException;

import java.io.File;

public class SaxonXQueryPerformerFactory extends SaxonActorFactory {

    @Override
    public SaxonActor newActor(boolean insecure) throws TransformationException {
        return new SaxonXQueryPerformer(insecure);
    }

    @Override
    public SaxonActor newActorWithConfig(File config) throws TransformationException {
        return new SaxonXQueryPerformer(config);
    }
}
