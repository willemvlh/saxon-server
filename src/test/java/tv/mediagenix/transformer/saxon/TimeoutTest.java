package tv.mediagenix.transformer.saxon;

import org.junit.jupiter.api.Test;
import tv.mediagenix.transformer.saxon.actors.SaxonActor;
import tv.mediagenix.transformer.saxon.actors.SaxonXQueryPerformerBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class TimeoutTest {

    @Test
    void timeout() {
        SaxonActor tf = new SaxonXQueryPerformerBuilder().setTimeout(1).build();
        String input = "for $i in 1 to 5000 \n" + "return (for $y in $i to 5000 return $y mod 4)";
        try {
            tf.act(new ByteArrayInputStream(input.getBytes()), new ByteArrayOutputStream());
            fail();
        } catch (TransformationException e) {
            assertTrue(e.getMessage().startsWith("Timeout exceeded"));
            assertTrue(e.getCause() instanceof TimeoutException);
        }
    }

    @Test
    void minusOneTimeout() {
        SaxonActor tf = new SaxonXQueryPerformerBuilder()
                .setTimeout(-1)
                .build();
        String input = "for $i in 1 to 2000 \n" + "return (for $y in $i to 1000 return $y mod 4)";
        try {
            tf.act(new ByteArrayInputStream(input.getBytes()), new ByteArrayOutputStream());
        } catch (TransformationException e) {
            fail(); //Should not throw a timeout
        }
    }

    @Test
    void negativeTimeout() {
        assertThrows(Exception.class, () -> new SaxonXQueryPerformerBuilder().setTimeout(-1000).build());
    }
}
