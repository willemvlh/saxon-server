package tv.mediagenix.xslt.transformer;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonTransformer;
import tv.mediagenix.xslt.transformer.server.ServerOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URISyntaxException;

public class ServerOptionsTest {
    @Test
    public void ParseTest() throws URISyntaxException, TransformationException {
        File configFile = new File(this.getClass().getResource("/tv/mediagenix/xslt/transformer/saxon-config.xml").toURI());
        SaxonTransformer xf = new SaxonTransformer(configFile);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        xf.act(TestHelpers.WellFormedXmlStream(), TestHelpers.SystemPropertyInvokingXslStream(), os);
        Assertions.assertNotEquals(os.size(), 0);
    }

    @Test
    public void NoOptionsParseTest() throws TransformationException {
        SaxonTransformer xf = new SaxonTransformer();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        xf.act(TestHelpers.WellFormedXmlStream(), TestHelpers.WellFormedXslStream(), os);
        Assertions.assertNotEquals(os.size(), 0);
    }

    @Test
    public void WrongConfigFileTest() {
        Assertions.assertThrows(TransformationException.class, () -> {
            SaxonTransformer xf = new SaxonTransformer(new File("unknown"));
        });
    }

    @Test
    public void DisallowExternalFunctionTest() throws URISyntaxException, TransformationException {
        //enabling the disallow-external-functions makes it impossible to access java system properties.
        File f = new File(this.getClass().getResource("/tv/mediagenix/xslt/transformer/saxon-config-no-external-fn.xml").toURI());
        SaxonTransformer xf = new SaxonTransformer(f);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        xf.act(TestHelpers.WellFormedXmlStream(), TestHelpers.SystemPropertyInvokingXslStream(), os);
        Assertions.assertEquals(os.size(), 0);
    }

    @Test
    public void SecureConfigurationTest() throws TransformationException {
        SaxonTransformer xf = new SaxonTransformer(false);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        xf.act(TestHelpers.WellFormedXmlStream(), TestHelpers.SystemPropertyInvokingXslStream(), os);
        Assertions.assertEquals(os.size(), 0);
        Assertions.assertThrows(TransformationException.class, () -> xf.act(
                TestHelpers.WellFormedXmlStream(),
                TestHelpers.xslWithDocAtURI(this.getClass().getResource("dummy.xml").toURI()),
                new ByteArrayOutputStream()));
    }

    @Test
    public void SetOptionsFromArgumentsTest() throws ParseException, URISyntaxException {
        String configFilePath = new File(this.getClass().getResource("/tv/mediagenix/xslt/transformer/saxon-config.xml").toURI()).getPath();
        String[] args = {"-port", "3000", "-config", configFilePath};
        ServerOptions opts = ServerOptions.fromArgs(args);
        Assertions.assertEquals(3000, (int) opts.getPort());
        Assertions.assertThrows(RuntimeException.class, () -> ServerOptions.fromArgs(new String[]{"-config", configFilePath, "-insecure"}));
        Assertions.assertTrue(ServerOptions.fromArgs(new String[]{"-insecure"}).isInsecure());
        Assertions.assertEquals(configFilePath, opts.getConfigFile().getPath());

    }


}

