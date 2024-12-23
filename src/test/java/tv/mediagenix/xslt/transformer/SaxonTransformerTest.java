package tv.mediagenix.xslt.transformer;

import org.junit.jupiter.api.Test;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonActor;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonTransformer;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonTransformerBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class SaxonTransformerTest {
    SaxonActor tf = new SaxonTransformerBuilder().setTimeout(5000).build();

    public SaxonTransformerTest() throws TransformationException {
    }

    @Test
    public void transformTest() throws UnsupportedEncodingException, TransformationException {
        ByteArrayOutputStream output = transformWithStrings(TestHelpers.WellFormedXml, TestHelpers.WellFormedXsl);
        assertEquals(output.toString("utf-8"), "hello", "The output should be 'hello'");
    }

    @Test
    public void parameters() throws TransformationException, UnsupportedEncodingException {
        SaxonActor actor = new SaxonTransformerBuilder().setParameters(Collections.singletonMap("myParam", "value")).build();
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
        transformWithStrings("<abc/>", TestHelpers.MessageInvokingXslNoTerminate);
    }

    @Test
    public void malformedXslTest() {
        assertThrows(TransformationException.class, () -> transformWithStrings(TestHelpers.MalformedXml, TestHelpers.WellFormedXsl), "Malformed input should trigger an exception");
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
    public void insecureTest() {
        SaxonTransformer xf = (SaxonTransformer) new SaxonTransformerBuilder().build();
        xf.setInsecure(true);
        assertDoesNotThrow(() -> xf.act(TestHelpers.WellFormedXmlStream(), Files.newInputStream(new File(this.getClass().getResource("test-dtd.xsl").toURI()).toPath()), new ByteArrayOutputStream()));
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
