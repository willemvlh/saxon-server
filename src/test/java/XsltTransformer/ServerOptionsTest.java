package XsltTransformer;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URISyntaxException;

public class ServerOptionsTest {
    @Test
    public void ParseTest() throws URISyntaxException, TransformationException {
        File configFile = new File(this.getClass().getResource("/XsltTransformer/saxon-config.xml").toURI());
        SaxonTransformer xf = new SaxonTransformer(configFile);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        xf.transform(TestHelpers.WellFormedXmlStream(), TestHelpers.SystemPropertyInvokingXslStream(), os);
        Assertions.assertNotEquals(os.size(), 0);
    }

    @Test
    public void NoOptionsParseTest() throws TransformationException {
        SaxonTransformer xf = new SaxonTransformer(null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        xf.transform(TestHelpers.WellFormedXmlStream(), TestHelpers.WellFormedXslStream(), os);
        Assertions.assertNotEquals(os.size(), 0);
    }

    @Test
    public void WrongConfigFileTest() {
        SaxonTransformer xf = new SaxonTransformer(new File("unknown"));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Assertions.assertThrows(TransformationException.class, () -> xf.transform(TestHelpers.WellFormedXmlStream(), TestHelpers.WellFormedXslStream(), os));
    }

    @Test
    public void DisallowExternalFunctionTest() throws URISyntaxException, TransformationException {
        //enabling the disallow-external-functions makes it impossible to access java system properties.
        File f = new File(this.getClass().getResource("/XsltTransformer/saxon-config-no-external-fn.xml").toURI());
        SaxonTransformer xf = new SaxonTransformer(f);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        xf.transform(TestHelpers.WellFormedXmlStream(), TestHelpers.SystemPropertyInvokingXslStream(), os);
        Assertions.assertEquals(os.size(), 0);
    }

    @Test
    public void SetOptionsFromArgumentsTest() throws ParseException, URISyntaxException {
        String configFilePath = new File(this.getClass().getResource("/XsltTransformer/saxon-config.xml").toURI()).getPath();
        String[] args = {"-port", "3000", "-config", configFilePath};
        ServerOptions opts = ServerOptions.fromArgs(args);
        Assertions.assertEquals(3000, (int) opts.getPort());
        Assertions.assertEquals(configFilePath, opts.getConfigFile().getPath());

    }


}

