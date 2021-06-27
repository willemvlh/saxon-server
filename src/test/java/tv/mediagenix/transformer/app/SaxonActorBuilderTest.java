package tv.mediagenix.transformer.app;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tv.mediagenix.transformer.TestHelpers;
import tv.mediagenix.transformer.saxon.TransformationException;
import tv.mediagenix.transformer.saxon.actors.SaxonTransformer;
import tv.mediagenix.transformer.saxon.actors.SaxonTransformerBuilder;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SaxonActorBuilderTest {

    @Test
    public void NoOptionsParseTest() throws TransformationException {
        SaxonTransformer actor = (SaxonTransformer) new SaxonTransformerBuilder().build();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        actor.act(TestHelpers.WellFormedXmlStream(), TestHelpers.WellFormedXslStream(), os);
        Assertions.assertNotEquals(os.size(), 0);
    }

    @Test
    public void ParseConfigFileTest() throws Exception {
        TransformerConfiguration config = new TransformerConfiguration("--config", this.getClass().getResource("/tv/mediagenix/transformer/app/saxon-config.xml").getPath());
        SaxonTransformer actor = (SaxonTransformer) new SaxonTransformerBuilder().setProcessor(config.getProcessor()).build();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        actor.act(TestHelpers.WellFormedXmlStream(), TestHelpers.SystemPropertyInvokingXslStream(), os);
        Assertions.assertNotEquals(os.size(), 0);
    }

    @Test
    public void WrongConfigFileTest() throws Exception {
        Assertions.assertThrows(TransformationException.class, () -> {
            new TransformerConfiguration("--config", "unknown").getProcessor();
        });
    }

    @Test
    public void DisallowExternalFunctionTest() throws ParseException, TransformationException {
        TransformerConfiguration config = new TransformerConfiguration("--config", this.getClass().getResource("/tv/mediagenix/transformer/app/saxon-config-no-external-fn.xml").getPath());
        SaxonTransformer actor = (SaxonTransformer) new SaxonTransformerBuilder().setProcessor(config.getProcessor()).build();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        actor.act(TestHelpers.WellFormedXmlStream(), TestHelpers.SystemPropertyInvokingXslStream(), os);
        assertEquals(os.size(), 0);
    }

   /* @Test
    public void SecureConfigurationTest() throws TransformationException {
        SaxonActor actor = new SaxonTransformerBuilder().setInsecure(false).build();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        actor.act(TestHelpers.WellFormedXmlStream(), TestHelpers.SystemPropertyInvokingXslStream(), os);
        assertEquals(os.size(), 0);
        Assertions.assertThrows(TransformationException.class, () -> actor.act(TestHelpers.WellFormedXmlStream(), TestHelpers.xslWithDocAtURI(this.getClass().getResource("dummy.xml").toURI()), new ByteArrayOutputStream()));
    }

    @Test
    public void InsecureConfigurationTest() throws TransformationException {
        SaxonActor actor = new SaxonTransformerBuilder().setInsecure(true).build();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        actor.act(TestHelpers.WellFormedXmlStream(), TestHelpers.SystemPropertyInvokingXslStream(), os);
        Assertions.assertNotEquals(os.size(), 0);
        Assertions.assertDoesNotThrow(() -> actor.act(TestHelpers.WellFormedXmlStream(), TestHelpers.xslWithDocAtURI(this.getClass().getResource("dummy.xml").toURI()), new ByteArrayOutputStream()));
    }

    */

}
