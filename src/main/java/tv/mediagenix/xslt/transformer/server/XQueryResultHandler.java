package tv.mediagenix.xslt.transformer.server;

import spark.Request;
import spark.Response;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonActorBuilder;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonXQueryPerformerBuilder;

public class XQueryResultHandler extends RequestHandler {

  public XQueryResultHandler(Request req, Response res) {
    super(req, res);
  }

  @Override
  protected SaxonActorBuilder newBuilder() {
    return new SaxonXQueryPerformerBuilder();
  }

}
