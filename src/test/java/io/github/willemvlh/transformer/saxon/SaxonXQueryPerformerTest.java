package io.github.willemvlh.transformer.saxon;

import io.github.willemvlh.transformer.TestHelpers;
import io.github.willemvlh.transformer.saxon.actors.SaxonActor;
import io.github.willemvlh.transformer.saxon.actors.SaxonXQueryPerformer;
import io.github.willemvlh.transformer.saxon.actors.SaxonXQueryPerformerBuilder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SaxonXQueryPerformerTest {

    private SaxonXQueryPerformer newXQueryPerformer() {
        return (SaxonXQueryPerformer) new SaxonXQueryPerformerBuilder().build();
    }

    @Test
    void act() throws TransformationException, UnsupportedEncodingException {
        SaxonXQueryPerformer p = newXQueryPerformer();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        p.act(TestHelpers.WellFormedXmlStream(), TestHelpers.WellFormedXQueryStream(), os);
        assertEquals("abc", os.toString("utf-8"));
    }

    @Test
    void actWithoutInput() throws TransformationException, UnsupportedEncodingException {
        SaxonXQueryPerformer p = newXQueryPerformer();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        p.act(TestHelpers.WellFormedXQueryStream(), os);
        assertEquals("abc", os.toString("utf-8"));

    }

    @Test
    void actFail() {
        SaxonXQueryPerformer p = newXQueryPerformer();
        assertThrows(TransformationException.class, () -> p.act(TestHelpers.WellFormedXmlStream(), TestHelpers.IncorrectXQueryStream(), new ByteArrayOutputStream()));
    }

    @Test
    void serializationProps() throws TransformationException {
        SaxonXQueryPerformer p = newXQueryPerformer();
        SerializationProps props = p.act(TestHelpers.WellFormedXmlStream(), TestHelpers.XQueryStreamApplicationJsonMime(), new ByteArrayOutputStream());
        assertEquals("utf-8", props.getEncoding().toLowerCase());
        assertEquals("application/json", props.getMime().toLowerCase());

    }

    @Test
    void parameters() throws TransformationException, UnsupportedEncodingException {
        SaxonActor p = new SaxonXQueryPerformerBuilder().setSerializationProperties(Collections.singletonMap("method", "text")).setParameters(Collections.singletonMap("myParam", "value")).build();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        p.act(getStream("declare variable $myParam external; $myParam"), os);
        assertEquals("value", os.toString("UTF-8"));
    }

    @Test
    void outputProperty() throws TransformationException, UnsupportedEncodingException {
        SaxonXQueryPerformer p = newXQueryPerformer();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        p.act(new ByteArrayInputStream("declare option saxon:output 'method=json'; map{}".getBytes()), os);
        assertTrue(os.toString("utf-8").startsWith("{"));
    }

    @Test
    void insecure() throws Exception {
        SaxonXQueryPerformer p = newXQueryPerformer();
        p.setSerializationParameters(Collections.singletonMap("method", "text"));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        p.act(getStream("available-environment-variables()"), os);
        assertTrue(os.toString().isEmpty());
        p.setInsecure();
        os = new ByteArrayOutputStream();
        p.act(getStream("available-environment-variables()"), os);
        assertFalse(os.toString().isEmpty());
        p.setSecure();
        os = new ByteArrayOutputStream();
        p.act(getStream("available-environment-variables()"), os);
        assertTrue(os.toString().isEmpty());

    }

    private InputStream getStream(String s) {
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }

}