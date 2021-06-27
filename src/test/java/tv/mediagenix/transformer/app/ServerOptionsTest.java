package tv.mediagenix.transformer.app;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import tv.mediagenix.transformer.saxon.TransformationException;
import tv.mediagenix.transformer.saxon.actors.SaxonTransformerBuilder;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class ServerOptionsTest {

    @Test
    public void SetOptionsFromArgumentsTest() throws ParseException, URISyntaxException {
        String configFilePath = new File(this.getClass().getResource("/tv/mediagenix/transformer/app/saxon-config.xml").toURI()).getPath();
        String[] args = {"-port", "3000", "-config", configFilePath};
        ServerOptions opts = ServerOptions.fromArgs(args);
        assertEquals(3000, opts.getPort());
        assertThrows(RuntimeException.class, () -> ServerOptions.fromArgs(new String[]{"-config", configFilePath, "-insecure"}));
        assertTrue(ServerOptions.fromArgs(new String[]{"-insecure"}).isInsecure());
        assertEquals(configFilePath, opts.getConfigFile().getPath());
    }

    @Test
    public void licenseTest() throws Exception {
        ApplicationArguments args = new DefaultApplicationArguments("--license", this.getClass().getResource("dummy-license.lic").getPath());
        TransformerConfiguration configuration = new TransformerConfiguration(args);
        SaxonTransformerBuilder b = new SaxonTransformerBuilder();
        assertThrows(TransformationException.class, configuration::getProcessor);
    }


}

