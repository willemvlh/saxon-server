package io.github.willemvlh.transformer.saxon.actors;

public class SaxonXQueryPerformerBuilder extends SaxonActorBuilder {
    @Override
    public Class<? extends SaxonActor> getActorClass() {
        return SaxonXQueryPerformer.class;
    }
}
