package tv.mediagenix.xslt.transformer.saxon;

import tv.mediagenix.xslt.transformer.saxon.actors.SaxonActor;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonXQueryPerformer;

import java.io.File;

public class SaxonXQueryPerformerFactory extends AbstractSaxonActorFactory {

    @Override
    public SaxonActor newActor(boolean insecure) throws TransformationException {
        return new SaxonXQueryPerformer(insecure);
    }

    @Override
    public SaxonActor newActorWithConfig(File config) throws TransformationException {
        return new SaxonXQueryPerformer(config);
    }
}
