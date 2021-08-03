package io.github.willemvlh.morph.saxon.actors;

public enum ActorType {

    TRANSFORM {
        @Override
        public SaxonActorBuilder getBuilder() {
            return new SaxonTransformerBuilder();
        }
    },

    QUERY {
        @Override
        public SaxonActorBuilder getBuilder() {
            return new SaxonXQueryPerformerBuilder();
        }
    };

    public abstract SaxonActorBuilder getBuilder();
}
