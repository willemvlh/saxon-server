package tv.mediagenix.transformer.saxon;

import org.junit.jupiter.api.Test;
import tv.mediagenix.transformer.saxon.actors.SaxonActor;
import tv.mediagenix.transformer.saxon.actors.SaxonXQueryPerformerBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

public class TimeoutTest {

    @Test
    public void timeout() {
        SaxonActor tf = new SaxonXQueryPerformerBuilder().setTimeout(1).build();
        String input = "for $i in 1 to 5000 \n" + "return (for $y in $i to 5000 return $y mod 4)";
        try {
            tf.act(new ByteArrayInputStream(input.getBytes()), new ByteArrayOutputStream());
            fail();
        } catch (TransformationException e) {
            System.out.println(e.getMessage());
            assertTrue(e.getCause() instanceof TimeoutException);
        }
    }

    @Test
    public void MinusOneTimeout() {
        SaxonActor tf = new SaxonXQueryPerformerBuilder()
                .setTimeout(-1)
                .build();
        String input = "for $i in 1 to 2000 \n" + "return (for $y in $i to 2000 return $y mod 4)";
        try {
            tf.act(new ByteArrayInputStream(input.getBytes()), new ByteArrayOutputStream());
        } catch (TransformationException e) {
            fail(); //Should not throw a timeout
        }
    }

    @Test
    public void negativeTimeout() {
        assertThrows(Exception.class, () -> new SaxonXQueryPerformerBuilder().setTimeout(-1000).build());
    }
}
