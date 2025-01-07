package tv.mediagenix.xslt.transformer;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import net.sf.saxon.Configuration;
import net.sf.saxon.trans.XPathException;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonActor;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonTransformer;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonTransformerBuilder;
import tv.mediagenix.xslt.transformer.server.Server;
import tv.mediagenix.xslt.transformer.server.ServerOptions;
import tv.mediagenix.xslt.transformer.server.ratelimiter.NoRateLimiter;
import tv.mediagenix.xslt.transformer.server.ratelimiter.RateLimiter;
import tv.mediagenix.xslt.transformer.server.ratelimiter.RateLimiterSettings;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerOptionsTest {

    SaxonActor actor = new SaxonTransformerBuilder().build();

    @Test
    public void ParseTest() throws URISyntaxException, TransformationException, XPathException {
        File configFile = new File(this.getClass().getResource("/tv/mediagenix/xslt/transformer/saxon-config.xml").toURI());
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
        File f = new File(this.getClass().getResource("/tv/mediagenix/xslt/transformer/saxon-config-no-external-fn.xml").toURI());
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
        Assertions.assertNotEquals(os.size(), 12);
        Assertions.assertDoesNotThrow(() -> actor.act(TestHelpers.WellFormedXmlStream(), TestHelpers.xslWithDocAtURI(this.getClass().getResource("dummy.xml").toURI()), new ByteArrayOutputStream()));
    }

    @Test
    public void LoggingTestCase() throws ParseException {
        ServerOptions opts = ServerOptions.fromArgs(new String[]{"-debug"});
        assertTrue(opts.isDebuggingEnabled());
        opts = ServerOptions.fromArgs(new String[]{"-d"});
        assertTrue(opts.isDebuggingEnabled());
    }

    @Test
    public void SetOptionsFromArgumentsTest() throws ParseException, URISyntaxException {
        String configFilePath = new File(this.getClass().getResource("/tv/mediagenix/xslt/transformer/saxon-config.xml").toURI()).getPath();
        String[] args = {"-port", "3000", "-config", configFilePath};
        ServerOptions opts = ServerOptions.fromArgs(args);
        assertEquals(3000, (int) opts.getPort());
        Assertions.assertThrows(RuntimeException.class, () -> ServerOptions.fromArgs(new String[]{"-config", configFilePath, "-insecure"}));
        assertTrue(ServerOptions.fromArgs(new String[]{"-insecure"}).isInsecure());
        assertEquals(configFilePath, opts.getConfigFile().getPath());

    }

    @Test
    public void rateLimitTest() throws ParseException {
        RateLimiterSettings rl1 = ServerOptions.fromArgs(new String[]{"--rate-limit", "light"}).getRateLimiter().getSettings();
        RateLimiterSettings rl2 = ServerOptions.fromArgs(new String[]{"--rate-limit", "heavy"}).getRateLimiter().getSettings();
        assertTrue(rl1.getMaxNumberOfRequests() / rl1.getSeconds() > rl2.getMaxNumberOfRequests() / rl2.getSeconds());
        RateLimiter rl3 = ServerOptions.fromArgs(new String[]{"--rate-limit", "none"}).getRateLimiter();
        for (int i = 0; i < 100; i++) {
            rl3.registerRequest("abc");
        }
        assertEquals(Duration.ZERO, rl3.timeToAllowed("abc"));
        assertEquals(ServerOptions.fromArgs(new String[]{}).getRateLimiter().getSettings(), new NoRateLimiter().getSettings());
    }

}

