package tv.mediagenix.transformer.app;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tv.mediagenix.transformer.TestHelpers;
import tv.mediagenix.transformer.saxon.TransformationException;
import tv.mediagenix.transformer.saxon.actors.SaxonActor;
import tv.mediagenix.transformer.saxon.actors.SaxonTransformer;
import tv.mediagenix.transformer.saxon.actors.SaxonTransformerBuilder;
import tv.mediagenix.transformer.saxon.config.SaxonDefaultConfigurationFactory;
import tv.mediagenix.transformer.saxon.config.SaxonSecureConfigurationFactory;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SaxonActorBuilderTest {

    private SaxonTransformer getTransformerWithConfiguration(Configuration c) {
        Processor p = new Processor(c);
        return (SaxonTransformer) new SaxonTransformerBuilder().setProcessor(p).build();
    }

    @Test
    void noOptionsParse() throws TransformationException {
        SaxonTransformer actor = (SaxonTransformer) new SaxonTransformerBuilder().build();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        actor.act(TestHelpers.WellFormedXmlStream(), TestHelpers.WellFormedXslStream(), os);
        Assertions.assertNotEquals(os.size(), 0);
    }

    @Test
    void parseConfigFile() throws Exception {
        TransformerConfiguration config = new TransformerConfiguration("--config", this.getClass().getResource("/tv/mediagenix/transformer/app/saxon-config.xml").getPath());
        SaxonTransformer actor = (SaxonTransformer) new SaxonTransformerBuilder().setProcessor(config.getProcessor()).build();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        actor.act(TestHelpers.WellFormedXmlStream(), TestHelpers.SystemPropertyInvokingXslStream(), os);
        Assertions.assertNotEquals(os.size(), 0);
    }

    @Test
    void wrongConfigFile() {
        Assertions.assertThrows(TransformationException.class, () -> {
            new TransformerConfiguration("--config", "unknown").getProcessor();
        });
    }

    @Test
    void disallowExternalFunction() throws ParseException, TransformationException {
        TransformerConfiguration config = new TransformerConfiguration("--config", this.getClass().getResource("/tv/mediagenix/transformer/app/saxon-config-no-external-fn.xml").getPath());
        SaxonTransformer actor = (SaxonTransformer) new SaxonTransformerBuilder().setProcessor(config.getProcessor()).build();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        actor.act(TestHelpers.WellFormedXmlStream(), TestHelpers.SystemPropertyInvokingXslStream(), os);
        assertEquals(os.size(), 0);
    }

    @Test
    void secureConfiguration() throws TransformationException {
        Configuration config = new SaxonSecureConfigurationFactory().newConfiguration();
        SaxonActor actor = getTransformerWithConfiguration(config);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        actor.act(TestHelpers.WellFormedXmlStream(), TestHelpers.SystemPropertyInvokingXslStream(), os);
        assertEquals(os.size(), 0);
        Assertions.assertThrows(TransformationException.class, () -> actor.act(TestHelpers.WellFormedXmlStream(), TestHelpers.xslWithDocAtURI(this.getClass().getResource("dummy.xml").toURI()), new ByteArrayOutputStream()));
    }

    @Test
    void insecureConfiguration() throws TransformationException {
        Configuration config = new SaxonDefaultConfigurationFactory().newConfiguration();
        SaxonActor actor = getTransformerWithConfiguration(config);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        actor.act(TestHelpers.WellFormedXmlStream(), TestHelpers.SystemPropertyInvokingXslStream(), os);
        Assertions.assertNotEquals(os.size(), 0);
        Assertions.assertDoesNotThrow(() -> actor.act(TestHelpers.WellFormedXmlStream(), TestHelpers.xslWithDocAtURI(this.getClass().getResource("dummy.xml").toURI()), new ByteArrayOutputStream()));
    }

}
