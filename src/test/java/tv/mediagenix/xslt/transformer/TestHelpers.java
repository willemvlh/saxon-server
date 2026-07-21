package tv.mediagenix.xslt.transformer;

import okhttp3.*;
import org.slf4j.LoggerFactory;
import spark.Spark;
import tv.mediagenix.xslt.transformer.server.Server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TestHelpers {
  public static String WellFormedXml = readResource("xml/dummy.xml");
  public static String WellFormedXsl = readResource("xsl/test-1.xsl");
  public static String WellformedXslWithInitialTemplate = readResource("xsl/test-initial-template.xsl");
  public static String message = "abc";
  public static String MessageInvokingXsl = readResource("xsl/test-message.xsl");
  public static String MessageInvokingXslNoTerminate = readResource("xsl/test-message-no-terminate.xsl");
  public static String MalformedXml = readResource("xml/malformed.xml");
  public static String XslWithParameters = readResource("xsl/test-parameters.xsl");
  public static String XslWithFile = readResource("xsl/test-file.xsl");

  public static InputStream WellFormedXslWithInitialTemplateStream() {
    return resourceStream("xsl/test-initial-template.xsl");
  }

  public static InputStream WellFormedXmlStream() {
    return resourceStream("xml/dummy.xml");
  }

  public static InputStream WellFormedXslStream() {
    return resourceStream("xsl/test-1.xsl");
  }

  public static InputStream WellFormedXQueryStream() {
    return resourceStream("xq/abc.xquery");
  }

  public static InputStream XQueryStreamApplicationJsonMime() {
    return resourceStream("xq/hof.xquery");
  }

  public static InputStream IncorrectXQueryStream() {
    return resourceStream("xq/syntax-error.xquery");
  }

  public static InputStream SystemPropertyInvokingXslStream() {
    return resourceStream("xsl/test-system-properties.xsl");
  }

  public static InputStream resourceStream(String name) {
    try (InputStream is = TestHelpers.class.getResourceAsStream(name)) {
      return new ByteArrayInputStream(is.readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static String readResource(String name) {
    try (InputStream is = resourceStream(name)) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static InputStream XslWithDocFunctionStream() {
    URL url = TestHelpers.class.getResource("xml/dummy.xml");
    String xsl = readResource("xsl/test-doc-fn.xsl").replace("{URI}", url.toString());
    return new ByteArrayInputStream(xsl.getBytes(StandardCharsets.UTF_8));
  }

  public static InputStream xslWithParameters() {
    return resourceStream("xsl/test-parameters.xsl");
  }

  public static void runServer(Runnable fn, String... args) {
    runServer(() -> {
      fn.run();
      return null;
    }, args);
  }

  public static <T> T runServer(Action<T> fn, String... args) {
    Server.main(args);
    Spark.awaitInitialization();
    LoggerFactory.getLogger(TestHelpers.class).debug("Started server.");
    T result = fn.run();
    Spark.stop();
    Spark.awaitStop();
    LoggerFactory.getLogger(TestHelpers.class).debug("Stopped server.");
    return result;
  }

  public static Response request(String xml, String xsl) {
    return new TestRequest().addXML(xml)
        .addXSL(xsl)
        .execute();
  }
}
