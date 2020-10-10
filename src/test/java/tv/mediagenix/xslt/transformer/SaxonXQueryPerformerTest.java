package tv.mediagenix.xslt.transformer;

import org.junit.jupiter.api.Test;
import tv.mediagenix.xslt.transformer.saxon.SerializationProps;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonXQueryPerformer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

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
        p.act(TestHelpers.WellFormedXQueryStream(), os);
        assertEquals("abc", os.toString("utf-8"));

    }

    @Test
    void actFail() {
        SaxonXQueryPerformer p = new SaxonXQueryPerformer();
        assertThrows(TransformationException.class, () -> p.act(TestHelpers.WellFormedXmlStream(), TestHelpers.IncorrectXQueryStream(), new ByteArrayOutputStream()));
    }

    @Test
    void serializationProps() throws TransformationException {
        SaxonXQueryPerformer p = new SaxonXQueryPerformer();
        SerializationProps props = p.act(TestHelpers.WellFormedXmlStream(), TestHelpers.XQueryStreamApplicationJsonMime(), new ByteArrayOutputStream());
        assertEquals("utf-8", props.getEncoding().toLowerCase());
        assertEquals("application/json", props.getMime().toLowerCase());

    }

    @Test
    void outputProperty() throws TransformationException, UnsupportedEncodingException {
        SaxonXQueryPerformer p = new SaxonXQueryPerformer();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        p.act(new ByteArrayInputStream("declare option saxon:output 'method=json'; map{}".getBytes()), os);
        assertTrue(os.toString("utf-8").startsWith("{"));
    }

}