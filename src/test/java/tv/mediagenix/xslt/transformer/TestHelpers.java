package tv.mediagenix.xslt.transformer;

import okhttp3.*;

import org.slf4j.LoggerFactory;

import spark.Spark;
import tv.mediagenix.xslt.transformer.saxon.core.TypedInputStream;
import tv.mediagenix.xslt.transformer.server.Server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Callable;

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
  public static String XslWithUnparsedTextFn = readResource("xsl/test-unparsed-text-fn.xsl");
  public static String XslWithJsonDocFn = readResource("xsl/test-json-doc-fn.xsl");
  public static String XslWithHttpRequest = readResource("xsl/test-http.xsl");

  public static TypedInputStream WellFormedXslWithInitialTemplateStream() {
    return resourceStream("xsl/test-initial-template.xsl");
  }

  public static TypedInputStream WellFormedXmlStream() {
    return resourceStream("xml/dummy.xml");
  }

  public static TypedInputStream WellFormedXslStream() {
    return resourceStream("xsl/test-1.xsl");
  }

  public static TypedInputStream WellFormedXQueryStream() {
    return resourceStream("xq/abc.xquery");
  }

  public static TypedInputStream XQueryStreamApplicationJsonMime() {
    return resourceStream("xq/hof.xquery");
  }

  public static TypedInputStream IncorrectXQueryStream() {
    return resourceStream("xq/syntax-error.xquery");
  }

  public static TypedInputStream SystemPropertyInvokingXslStream() {
    return resourceStream("xsl/test-system-properties.xsl");
  }

  public static TypedInputStream resourceStream(String name) {
    try (InputStream is = TestHelpers.class.getResourceAsStream(name)) {
      return new TypedInputStream(new ByteArrayInputStream(is.readAllBytes()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static String readResource(String name) {
    try (InputStream is = resourceStream(name).getInputStream()) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static TypedInputStream XslWithDocFunctionStream() {
    URL url = TestHelpers.class.getResource("xml/dummy.xml");
    String xsl = readResource("xsl/test-doc-fn.xsl").replace("{URI}", url.toString());
    return new TypedInputStream(new ByteArrayInputStream(xsl.getBytes(StandardCharsets.UTF_8)));
  }

  public static TypedInputStream xslWithParameters() {
    return resourceStream("xsl/test-parameters.xsl");
  }

  public static void runServer(Runnable fn, String... args) {
    runServer(() -> {
      fn.run();
      return null;
    }, args);
  }

  public static <T> T runServerDebug(Callable<T> fn, String... args) {
    var argsWithDebug = Arrays.copyOf(args, args.length + 1);
    argsWithDebug[args.length] = "--debug";
    return runServer(fn, argsWithDebug);
  }

  public static <T> T runServer(Callable<T> fn, String... args) {
    Server.main(args);
    Spark.awaitInitialization();
    LoggerFactory.getLogger(TestHelpers.class).debug("Started server.");
    try {
      T result = fn.call();
      Spark.stop();
      Spark.awaitStop();
      LoggerFactory.getLogger(TestHelpers.class).debug("Stopped server.");
      return result;
    } catch (Exception e) {
      LoggerFactory.getLogger(TestHelpers.class).error("Error occurred during server execution, e");
      return null;
    }
  }

  public static Response request(String xml, String xsl) {
    return new TestRequest().addXML(xml)
        .addXSL(xsl)
        .execute();
  }
}
