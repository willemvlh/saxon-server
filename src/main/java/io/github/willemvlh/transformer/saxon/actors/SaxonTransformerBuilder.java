package io.github.willemvlh.transformer.saxon.actors;

public class SaxonTransformerBuilder extends SaxonActorBuilder {

    @Override
    public Class<? extends SaxonActor> getActorClass() {
        return SaxonTransformer.class;
    }
}
