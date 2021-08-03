package io.github.willemvlh.morph.app;

import io.github.willemvlh.morph.saxon.TransformationException;
import io.github.willemvlh.morph.saxon.actors.SaxonActor;
import io.github.willemvlh.morph.saxon.actors.SaxonTransformerBuilder;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class ServerOptionsTest {

    @Test
    void setOptionsFromArguments() throws ParseException, URISyntaxException {
        String configFilePath = new File(this.getClass().getResource("/io/github/willemvlh/harley/app/saxon-config.xml").toURI()).getPath();
        String[] args = {"-port", "3000", "-config", configFilePath};
        ServerOptions opts = ServerOptions.fromArgs(args);
        assertEquals(3000, opts.getPort());
        assertThrows(RuntimeException.class, () -> ServerOptions.fromArgs(new String[]{"-config", configFilePath, "-insecure"}));
        assertTrue(ServerOptions.fromArgs("-insecure").isInsecure());
        assertEquals(configFilePath, opts.getConfigFile().getPath());
    }

    @Test
    void timeout() throws ParseException {
        ServerOptions opts = ServerOptions.fromArgs("--timeout", "100");
        SaxonActor actor = new SaxonTransformerBuilder().setTimeout(opts.getTransformationTimeoutMs()).build();
        assertEquals(100, actor.getTimeout());
    }

    @Test
    void license() throws Exception {
        ApplicationArguments args = new DefaultApplicationArguments("--license", this.getClass().getResource("dummy-license.lic").getPath());
        TransformerConfiguration configuration = new TransformerConfiguration(args);
        SaxonTransformerBuilder b = new SaxonTransformerBuilder();
        assertThrows(TransformationException.class, configuration::getProcessor);
    }


}

