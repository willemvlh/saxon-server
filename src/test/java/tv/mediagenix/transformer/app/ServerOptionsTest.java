package tv.mediagenix.transformer.app;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerOptionsTest {

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

