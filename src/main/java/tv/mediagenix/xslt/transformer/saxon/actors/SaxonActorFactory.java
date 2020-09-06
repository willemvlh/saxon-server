package tv.mediagenix.xslt.transformer.saxon.actors;

import tv.mediagenix.xslt.transformer.saxon.TransformationException;

import java.io.File;

public abstract class SaxonActorFactory {
    public abstract SaxonActor newActor(boolean insecure) throws TransformationException;

    public abstract SaxonActor newActorWithConfig(File config) throws TransformationException;
}

