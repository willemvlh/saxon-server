package io.github.willemvlh.morph.saxon.actors;

public class SaxonTransformerBuilder extends SaxonActorBuilder {

    @Override
    public Class<? extends SaxonActor> getActorClass() {
        return SaxonTransformer.class;
    }
}
