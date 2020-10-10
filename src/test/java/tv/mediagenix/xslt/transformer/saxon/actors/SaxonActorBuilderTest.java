package tv.mediagenix.xslt.transformer.saxon.actors;

import net.sf.saxon.lib.Feature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SaxonActorBuilderTest {

    @Test
    public void testBuilder() {
        SaxonTransformerBuilder b = new SaxonTransformerBuilder();
        b.setInsecure(true);
        SaxonActor tf = b.build();
        assertEquals(true, tf.getProcessor().getConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS));

        b.setInsecure(false);
        tf = b.build();
        assertEquals(false, tf.getProcessor().getConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS));
    }
}