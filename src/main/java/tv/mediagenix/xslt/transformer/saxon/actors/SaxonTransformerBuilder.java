package tv.mediagenix.xslt.transformer.saxon.actors;

public class SaxonTransformerBuilder extends SaxonActorBuilder {

  @Override
  protected SaxonActor createNewInstance() {
    return new SaxonTransformer();
  }

}
