package io.github.willemvlh.transformer.saxon;

import io.github.willemvlh.transformer.TestHelpers;
import io.github.willemvlh.transformer.saxon.actors.SaxonActor;
import io.github.willemvlh.transformer.saxon.actors.SaxonTransformer;
import io.github.willemvlh.transformer.saxon.actors.SaxonTransformerBuilder;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SaxonTransformerTest {
    private final SaxonActor tf = new SaxonTransformerBuilder().setTimeout(5000).build();

    @Test
    void transform() throws UnsupportedEncodingException, TransformationException {
        ByteArrayOutputStream output = transformWithStrings(TestHelpers.WellFormedXml, TestHelpers.WellFormedXsl);
        assertEquals(output.toString("utf-8"), "hello", "The output should be 'hello'");
    }

    @Test
    void parameters() throws TransformationException, UnsupportedEncodingException {
        SaxonActor actor = new SaxonTransformerBuilder().setParameters(Collections.singletonMap("myParam", "value")).build();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        actor.act(getStream("<abc/>"), TestHelpers.xslWithParameters(), output);
        assertEquals("value", output.toString("UTF-8"));
    }

    @Test
    void transformWithoutInput() throws TransformationException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        tf.act(TestHelpers.WellFormedXslWithInitialTemplateStream(), os);
        assertEquals("hello", os.toString());
    }

    @Test
    void malformedXsl() {
        assertThrows(TransformationException.class, () -> transformWithStrings(TestHelpers.MalformedXml, TestHelpers.WellFormedXsl), "Malformed input should trigger an exception");
    }

    @Test
    void errorMsg() {
        try {
            transformWithStrings("bad xml", "bad xsl");
        } catch (TransformationException e) {
            assertTrue(e.getMessage().contains("a"));
        }
    }

    @Test
    void message() {
        try {
            transformWithStrings("<x/>", TestHelpers.MessageInvokingXsl);
            fail("should have thrown");
        } catch (TransformationException e) {
            assertEquals(TestHelpers.message, e.getMessage());
        }

    }

    @Test
    void testWithCompilationError() {
        try {
            transformWithStrings("<abc/>", "<xsl:template xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"/>");
            fail();
        } catch (TransformationException e) {
            assertTrue(e.getMessage().contains("Compilation error"));
        }
    }

    @Test
    void insecure() {
        SaxonTransformer xf = (SaxonTransformer) new SaxonTransformerBuilder().build();
        xf.setInsecure();
        assertDoesNotThrow(() -> xf.act(TestHelpers.WellFormedXmlStream(), new FileInputStream(new File(this.getClass().getResource("test-dtd.xsl").toURI())), new ByteArrayOutputStream()));
    }

    @Test
    void transformBomEncoded() throws UnsupportedEncodingException, TransformationException {
        ByteArrayOutputStream output = transformWithStrings(TestHelpers.WellFormedBomXml, TestHelpers.WellFormedXsl);
        assertEquals("hello", output.toString(StandardCharsets.UTF_8), "The output should be 'hello'");
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
