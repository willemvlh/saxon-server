package tv.mediagenix.xslt.transformer;

import org.junit.jupiter.api.Test;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonXQueryPerformer;

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
    void actFail() throws TransformationException {
        SaxonXQueryPerformer p = new SaxonXQueryPerformer();
        assertThrows(TransformationException.class,
                () -> p.act(TestHelpers.WellFormedXmlStream(), TestHelpers.IncorrectXQueryStream(), new ByteArrayOutputStream()));
    }

    @Test
    void serializationProps() throws TransformationException {
        SaxonXQueryPerformer p = new SaxonXQueryPerformer();
        SerializationProperties props = p.act(TestHelpers.WellFormedXmlStream(), TestHelpers.WellFormedXQueryStream(), new ByteArrayOutputStream());
        assertTrue(props.contentType().toLowerCase().contains("utf-8"));
    }

}