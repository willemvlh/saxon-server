package tv.mediagenix.xslt.transformer.saxon.actors;

public class SaxonXQueryPerformerBuilder extends SaxonActorBuilder {

  @Override
  protected SaxonActor createNewInstance() {
    return new SaxonXQueryPerformer();
  }
}
