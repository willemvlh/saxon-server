package tv.mediagenix.xslt.transformer;

import org.junit.jupiter.api.Test;

import tv.mediagenix.xslt.transformer.saxon.actors.SaxonActor;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonXQueryPerformer;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonXQueryPerformerBuilder;
import tv.mediagenix.xslt.transformer.saxon.core.SerializationProps;
import tv.mediagenix.xslt.transformer.saxon.core.TransformationException;
import tv.mediagenix.xslt.transformer.saxon.core.TypedInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SaxonXQueryPerformerTest {

  @Test
  void act() throws TransformationException, UnsupportedEncodingException {
    SaxonXQueryPerformer p = new SaxonXQueryPerformer();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    p.act(TestHelpers.WellFormedXmlStream(), TestHelpers.WellFormedXQueryStream(), os);
    assertEquals("abc", os.toString("utf-8"));
  }

  @Test
  void actWithoutInput() throws TransformationException, UnsupportedEncodingException {
    SaxonXQueryPerformer p = new SaxonXQueryPerformer();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    p.act(null, TestHelpers.WellFormedXQueryStream(), os);
    assertEquals("abc", os.toString("utf-8"));

  }

  @Test
  void actFail() {
    SaxonXQueryPerformer p = new SaxonXQueryPerformer();
    assertThrows(TransformationException.class, () -> p.act(TestHelpers.WellFormedXmlStream(),
        TestHelpers.IncorrectXQueryStream(), new ByteArrayOutputStream()));
  }

  @Test
  void serializationProps() throws TransformationException {
    SaxonXQueryPerformer p = new SaxonXQueryPerformer();
    SerializationProps props = p.act(TestHelpers.WellFormedXmlStream(), TestHelpers.XQueryStreamApplicationJsonMime(),
        new ByteArrayOutputStream());
    assertEquals("utf-8", props.getEncoding().toLowerCase());
    assertEquals("application/json", props.getMime().toLowerCase());

  }

  @Test
  void parameters() throws TransformationException, UnsupportedEncodingException {
    SaxonActor p = new SaxonXQueryPerformerBuilder()
        .setSerializationProperties(Collections.singletonMap("method", "text"))
        .setParameters(Collections.singletonMap("myParam", "value")).build();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    p.act(null, getStream("declare variable $myParam external; $myParam"), os);
    assertEquals("value", os.toString("UTF-8"));
  }

  @Test
  void outputProperty() throws TransformationException, UnsupportedEncodingException {
    SaxonXQueryPerformer p = new SaxonXQueryPerformer();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    p.act(null, getStream("declare option saxon:output 'method=json'; map{}"), os);
    assertTrue(os.toString("utf-8").startsWith("{"));
  }

  @Test
  void setBaseURI() throws TransformationException, UnsupportedEncodingException {
    SaxonXQueryPerformer p = new SaxonXQueryPerformer();
    p.setBaseURI(URI.create("file:///tmp/"));
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    p.act(null, getStream("resolve-uri('test.xml')"), os);
    assertTrue(os.toString("UTF-8").contains("/tmp/test.xml"));
  }

  @Test
  void files() throws TransformationException {
    SaxonXQueryPerformer p = (SaxonXQueryPerformer) new SaxonXQueryPerformerBuilder()
        .setFiles(Collections.singletonMap("test.xml", getStream("<abc>Expected value</abc>").getInputStream())).build();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    p.setSerializationParameters(Collections.singletonMap("method", "text"));
    p.act(null, getStream("doc('test.xml')/abc"), os);
    assertEquals("Expected value", os.toString(StandardCharsets.UTF_8));
  }

  private TypedInputStream getStream(String s) {
    return new TypedInputStream(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)), "text/query");
  }

}
