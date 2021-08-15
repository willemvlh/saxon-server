package io.github.willemvlh.transformer.app;

import io.github.willemvlh.transformer.saxon.actors.SaxonActor;
import io.github.willemvlh.transformer.saxon.actors.SaxonTransformerBuilder;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class ServerOptionsTest {

    @Test
    void port() throws ParseException {
        ServerOptions opts = ServerOptions.fromArgs("--port", "3000");
        assertEquals(3000, opts.getPort());
    }

    @Test
    void configFile() throws ParseException, URISyntaxException {
        String configFilePath = new File(this.getClass().getResource("saxon-config.xml").toURI()).getPath();
        ServerOptions opts = ServerOptions.fromArgs("-config", configFilePath);
        assertEquals(configFilePath, opts.getConfigFile().getPath());
    }

    @Test
    void insecure() throws ParseException {
        assertTrue(ServerOptions.fromArgs("-insecure").isInsecure());
    }

    @Test
    void insecureAndConfigFileAreMutuallyExclusive() throws URISyntaxException {
        String configFilePath = new File(this.getClass().getResource("saxon-config.xml").toURI()).getPath();
        assertThrows(RuntimeException.class, () -> ServerOptions.fromArgs(new String[]{"-config", configFilePath, "-insecure"}));
    }

    @Test
    void invalidPort() {
        assertThrows(ParseException.class, () -> ServerOptions.fromArgs("--port", "-111"));
    }

    @Test
    void invalidTimeout() {
        assertThrows(ParseException.class, () -> ServerOptions.fromArgs("--timeout", "-111"));
        assertThrows(ParseException.class, () -> ServerOptions.fromArgs("--timeout", "10000000000000000000000000000000"));
        assertDoesNotThrow(() -> ServerOptions.fromArgs("--timeout", "-1"));
    }

    @Test
    void timeout() throws ParseException {
        ServerOptions opts = ServerOptions.fromArgs("--timeout", "100");
        SaxonActor actor = new SaxonTransformerBuilder().setTimeout(opts.getTransformationTimeoutMs()).build();
        assertEquals(100, actor.getTimeout());
    }

    @Test
    void help() throws Exception {
        ApplicationArguments args = new DefaultApplicationArguments("--help");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ServerOptions.fromArgs(os, false, args.getSourceArgs());
        System.out.println(os.toString());
        assertFalse(os.toString().isEmpty());
    }

    @Test
    void info() throws Exception {
        ApplicationArguments args = new DefaultApplicationArguments("--version");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ServerOptions.fromArgs(os, false, args.getSourceArgs());
        System.out.println(os.toString());
        assertTrue(os.toString().trim().matches("Saxon HE [\\d.]*"));
    }

    @Test
    void output() throws Exception {
        ServerOptions opts = ServerOptions.fromArgs("--output", "c:\\temp\\log.log");
        assertEquals("c:\\temp\\log.log", opts.getLogFilePath());
    }

}

