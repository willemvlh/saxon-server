package io.github.willemvlh.transformer.saxon.actors;

import io.github.willemvlh.transformer.app.TransformerConfiguration;
import net.sf.saxon.lib.Feature;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SaxonActorBuilderTest {

    @Test
    void setProcessor() throws Exception {
        ApplicationArguments args = new DefaultApplicationArguments("--insecure");
        TransformerConfiguration configuration = new TransformerConfiguration(args);
        SaxonTransformerBuilder b = new SaxonTransformerBuilder();
        b.setProcessor(configuration.getProcessor());
        SaxonActor tf = b.build();
        assertEquals(true, tf.getProcessor().getConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS));

        args = new DefaultApplicationArguments();
        configuration = new TransformerConfiguration(args);
        b = new SaxonTransformerBuilder();
        tf = b.setProcessor(configuration.getProcessor()).build();
        assertEquals(false, tf.getProcessor().getConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS));
    }
}