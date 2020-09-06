package tv.mediagenix.xslt.transformer.saxon;

import tv.mediagenix.xslt.transformer.saxon.actors.SaxonActor;

import java.io.File;

public abstract class AbstractSaxonActorFactory {
    public abstract SaxonActor newActor(boolean insecure) throws TransformationException;

    public abstract SaxonActor newActorWithConfig(File config) throws TransformationException;
}

