package tv.mediagenix.transformer.saxon.actors;

public class SaxonXQueryPerformerBuilder extends SaxonActorBuilder {
    @Override
    public Class<? extends SaxonActor> getActorClass() {
        return SaxonXQueryPerformer.class;
    }
}
