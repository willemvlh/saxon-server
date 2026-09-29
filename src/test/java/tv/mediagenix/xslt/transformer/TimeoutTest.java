package tv.mediagenix.xslt.transformer;

import org.junit.jupiter.api.Test;

import tv.mediagenix.xslt.transformer.saxon.actors.SaxonActor;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonXQueryPerformerBuilder;
import tv.mediagenix.xslt.transformer.saxon.core.TransformationException;
import tv.mediagenix.xslt.transformer.saxon.core.TypedInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeoutTest {

    @Test
    void timeout() {
        SaxonActor tf = new SaxonXQueryPerformerBuilder().setTimeout(1).build();
        String input = "for $i in 1 to 5000 \n" + "return (for $y in $i to 5000 return $y mod 4)";
        TransformationException e = assertThrows(TransformationException.class,
            () -> tf.act(null, new TypedInputStream(new ByteArrayInputStream(input.getBytes())), new ByteArrayOutputStream()));
        System.out.println(e.getMessage());
        assertTrue(e.getCause() instanceof TimeoutException);
    }
}
