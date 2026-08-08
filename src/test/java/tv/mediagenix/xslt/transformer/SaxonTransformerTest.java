package tv.mediagenix.xslt.transformer;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonActor;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonTransformer;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonTransformerBuilder;
import tv.mediagenix.xslt.transformer.saxon.core.TransformationException;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class SaxonTransformerTest {
  SaxonActor tf = new SaxonTransformerBuilder().setTimeout(5000).build();

  @Test
  public void transformTest() throws UnsupportedEncodingException, TransformationException {
    ByteArrayOutputStream output = transformWithStrings(TestHelpers.WellFormedXml, TestHelpers.WellFormedXsl);
    assertEquals(output.toString("utf-8"), "hello", "The output should be 'hello'");
  }

  @Test
  public void parameters() throws TransformationException, UnsupportedEncodingException {
    SaxonActor actor = new SaxonTransformerBuilder().setParameters(Collections.singletonMap("myParam", "value"))
        .build();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    actor.act(getStream("<abc/>"), TestHelpers.xslWithParameters(), output);
    assertEquals("value", output.toString("UTF-8"));
  }

  @Test
  public void transformWithoutInputTest() throws TransformationException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    tf.act(TestHelpers.WellFormedXslWithInitialTemplateStream(), os);
    assertEquals("hello", os.toString());
  }

  @Test
  public void messageNoTerminate() throws TransformationException {
    var logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(SaxonTransformer.class);
    var appender = new ListAppender<ILoggingEvent>();
    appender.start();
    logger.addAppender(appender);
    transformWithStrings("<abc/>", TestHelpers.MessageInvokingXslNoTerminate);
    var messages = appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    assertTrue(messages.stream().anyMatch(msg -> msg.contains("Message received: \"abc\". Line: 3, column: 37")));
  }

  @Test
  public void malformedXslTest() {
    assertThrows(TransformationException.class,
        () -> transformWithStrings(TestHelpers.MalformedXml, TestHelpers.WellFormedXsl),
        "Malformed input should trigger an exception");
  }

  @Test
  public void errorMsgTest() {
    try {
      transformWithStrings("bad xml", "bad xsl");
    } catch (TransformationException e) {
      assertTrue(e.getMessage().contains("a"));
    }
  }

  @Test
  public void messageTest() {
    try {
      transformWithStrings("<x/>", TestHelpers.MessageInvokingXsl);
      fail("should have thrown");
    } catch (TransformationException e) {
      assertEquals(TestHelpers.message, e.getMessage());
    }

  }

  @Test
  public void testWithCompilationError() {
    try {
      transformWithStrings("<abc/>", "<xsl:template xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"/>");
      fail();
    } catch (TransformationException e) {
      System.out.println(e.getMessage());
      ;
    }
    ;
  }

  @Test
  public void setBaseURI() throws TransformationException {
    SaxonActor actor = new SaxonTransformerBuilder().setBaseURI(URI.create("http://www.google.com")).build();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    String xsl = "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"3.0\">" +
        "<xsl:output method=\"text\"/>" +
        "<xsl:template name=\"xsl:initial-template\">" +
        "<xsl:value-of select=\"resolve-uri('test.xml') || '_' || static-base-uri()\"/>" +
        "</xsl:template>" +
        "</xsl:stylesheet>";
    actor.act(getStream(xsl), os);
    assertEquals("http://www.google.com/test.xml_http://www.google.com", os.toString());
  }

  @Test
  public void files() throws TransformationException {
    SaxonActor actor = new SaxonTransformerBuilder().setFiles(Collections.singletonMap("test.xml", getStream("<abc>Expected value</abc>")))
        .build();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    String xsl = "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"3.0\">" +
        "<xsl:output method=\"text\"/>" +
        "<xsl:template name=\"xsl:initial-template\">" +
        "<xsl:variable name=\"doc\" select=\"doc('test.xml')\"/>" +
        "<xsl:value-of select=\"$doc/abc\"/>" +
        "</xsl:template>" +
        "</xsl:stylesheet>";
    actor.act(getStream(xsl), os);
    assertEquals("Expected value", os.toString());
  }

  @Test
  public void insecureTest() {
    SaxonTransformer xf = (SaxonTransformer) new SaxonTransformerBuilder().build();
    xf.setInsecure(true);
    assertDoesNotThrow(() -> xf.act(TestHelpers.WellFormedXmlStream(), TestHelpers.resourceStream("xsl/test-dtd.xsl"),
        new ByteArrayOutputStream()));
  }

  @Test
  public void collectionTest(){
    SaxonTransformer xf = new SaxonTransformer();
    xf.setInsecure(false);
    assertThrows(TransformationException.class, () -> xf.act(TestHelpers.WellFormedXmlStream(), TestHelpers.resourceStream("xsl/test-collection.xsl"),
        new ByteArrayOutputStream()));
    xf.setInsecure(true);
    assertDoesNotThrow(() -> xf.act(TestHelpers.WellFormedXmlStream(), TestHelpers.resourceStream("xsl/test-collection.xsl"),
        new ByteArrayOutputStream()));
  }

  @Test
  public void testIncludeSecure() throws URISyntaxException {
    SaxonActor actor = new SaxonTransformerBuilder()
      .setInsecure(false)
            .setBaseURI(this.getClass().getResource("xsl").toURI())
      .build();
    assertThrows(TransformationException.class, () -> actor.act(TestHelpers.resourceStream("xsl/test-include.xsl"), new ByteArrayOutputStream()));
  }

  @Test
  public void testIncludeInsecure() throws URISyntaxException, TransformationException {
    SaxonActor actor = new SaxonTransformerBuilder()
            .setInsecure(true)
            .setTimeout(3600*1000)
            .setBaseURI(this.getClass().getResource("xsl/").toURI())
            .build();
    actor.act(TestHelpers.resourceStream("xsl/test-include.xsl"), new ByteArrayOutputStream());
  }

  @Test
  public void testIncludeFromAttachedFile() throws TransformationException {
    SaxonActor actor = new SaxonTransformerBuilder()
      .setFiles(Collections.singletonMap("included.xsl", TestHelpers.resourceStream("xsl/included.xsl")))
      .build();
    var output = new ByteArrayOutputStream();
    actor.act(TestHelpers.resourceStream("xsl/test-include.xsl"), output);
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>Hello, world!", output.toString());
  }

  private ByteArrayOutputStream transformWithStrings(String xml, String xsl) throws TransformationException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    InputStream input = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    InputStream xslStr = new ByteArrayInputStream(xsl.getBytes(StandardCharsets.UTF_8));
    tf.act(input, xslStr, output);
    return output;
  }

  private InputStream getStream(String s) {
    return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
  }
}
