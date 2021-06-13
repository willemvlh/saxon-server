package tv.mediagenix.transformer.app;

import net.sf.saxon.Configuration;
import net.sf.saxon.trans.XPathException;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tv.mediagenix.transformer.TestHelpers;
import tv.mediagenix.transformer.saxon.TransformationException;
import tv.mediagenix.transformer.saxon.actors.SaxonActor;
import tv.mediagenix.transformer.saxon.actors.SaxonTransformer;
import tv.mediagenix.transformer.saxon.actors.SaxonTransformerBuilder;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerOptionsTest {

    SaxonActor actor = new SaxonTransformerBuilder().build();

    @Test
    public void ParseTest() throws URISyntaxException, TransformationException, XPathException {
        File configFile = new File(this.getClass().getResource("/tv/mediagenix/transformer/app/saxon-config.xml").toURI());
        actor.setConfiguration(Configuration.readConfiguration(new StreamSource(configFile)));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        actor.act(TestHelpers.WellFormedXmlStream(), TestHelpers.SystemPropertyInvokingXslStream(), os);
        Assertions.assertNotEquals(os.size(), 0);
    }

    @Test
    public void NoOptionsParseTest() throws TransformationException {
        SaxonTransformer actor = (SaxonTransformer) new SaxonTransformerBuilder().build();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        actor.act(TestHelpers.WellFormedXmlStream(), TestHelpers.WellFormedXslStream(), os);
        Assertions.assertNotEquals(os.size(), 0);
    }

    @Test
    public void WrongConfigFileTest() {
        Assertions.assertThrows(TransformationException.class, () -> {
            new SaxonTransformerBuilder().setConfigurationFile(new File("unknown")).build();
        });
    }

    @Test
    public void DisallowExternalFunctionTest() throws URISyntaxException, TransformationException {
        //enabling the disallow-external-functions makes it impossible to access java system properties.
        File f = new File(this.getClass().getResource("/tv/mediagenix/transformer/app/saxon-config-no-external-fn.xml").toURI());
        SaxonActor actor = new SaxonTransformerBuilder().setConfigurationFile(f).build();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        actor.act(TestHelpers.WellFormedXmlStream(), TestHelpers.SystemPropertyInvokingXslStream(), os);
        assertEquals(os.size(), 0);
    }

    @Test
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

    @Test
    public void SetOptionsFromArgumentsTest() throws ParseException, URISyntaxException {
        String configFilePath = new File(this.getClass().getResource("/tv/mediagenix/transformer/app/saxon-config.xml").toURI()).getPath();
        String[] args = {"-port", "3000", "-config", configFilePath};
        ServerOptions opts = ServerOptions.fromArgs(args);
        assertEquals(3000, (int) opts.getPort());
        Assertions.assertThrows(RuntimeException.class, () -> ServerOptions.fromArgs(new String[]{"-config", configFilePath, "-insecure"}));
        assertTrue(ServerOptions.fromArgs(new String[]{"-insecure"}).isInsecure());
        assertEquals(configFilePath, opts.getConfigFile().getPath());

    }


}

