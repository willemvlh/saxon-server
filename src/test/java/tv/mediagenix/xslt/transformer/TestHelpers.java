package tv.mediagenix.xslt.transformer;

import okhttp3.*;
import org.slf4j.LoggerFactory;
import spark.Spark;
import tv.mediagenix.xslt.transformer.server.Server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class TestHelpers {
    public static String WellFormedXml = "<root/>";
    public static String WellFormedXsl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"3.0\">\n" + "    <xsl:output method=\"text\"/>\n" + "    <xsl:template match=\"/\">\n" + "        <xsl:text>hello</xsl:text>\n" + "    </xsl:template>\n" + "</xsl:stylesheet>";
    public static String WellformedXslWithInitialTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"3.0\">\n" + "    <xsl:output method=\"text\"/>\n" + "    <xsl:template name=\"xsl:initial-template\">\n" + "        <xsl:text>hello</xsl:text>\n" + "    </xsl:template>\n" + "</xsl:stylesheet>";
    public static String message = "abc";
    public static String MessageInvokingXsl = "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"3.0\"><xsl:template match=\"/\"><xsl:message terminate=\"yes\">" + message + "</xsl:message></xsl:template></xsl:stylesheet>";
    public static String MessageInvokingXslNoTerminate = "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"3.0\"><xsl:template match=\"/\"><xsl:message terminate=\"no\">" + message + "</xsl:message></xsl:template></xsl:stylesheet>";
    public static String MalformedXml = "noXml";
    public static String XslWithParameters = "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"3.0\">\n" + "\t<xsl:output method=\"text\"/>\n" + "<xsl:param name=\"myParam\"/>\n" + "\t<xsl:template match=\"/\">\n" + "\t\t<xsl:value-of select=\"$myParam\"/>\n" + "\t</xsl:template>\n" + "</xsl:stylesheet>";

    public static InputStream WellFormedXslWithInitialTemplateStream() {
        return getInputStreamFromUtf8String(WellformedXslWithInitialTemplate);
    }

    public static InputStream WellFormedXmlStream() {
        return getInputStreamFromUtf8String(WellFormedXml);
    }

    public static InputStream WellFormedXslStream() {
        return getInputStreamFromUtf8String(WellFormedXsl);
    }

    public static InputStream WellFormedXQueryStream() {
        return getInputStreamFromUtf8String("declare option saxon:output 'method=text';'abc'");
    }

    public static InputStream XQueryStreamApplicationJsonMime() {
        return getInputStreamFromUtf8String("declare option saxon:output 'media-type=application/json';  declare option saxon:output 'method=json'; map{'a':'b'}");
    }

    public static InputStream IncorrectXQueryStream() {
        return getInputStreamFromUtf8String("let $x = \"abc\";\n" + "return $x;");
    }

    public static InputStream SystemPropertyInvokingXslStream() {
        return getInputStreamFromUtf8String("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"3.0\">\n" + "\t<xsl:output method=\"text\"/>\n" + "\t<xsl:template match=\"/\">\n" + "\t\t<xsl:value-of select=\"system-property('java.home')\"/>\n" + "\t</xsl:template>\n" + "</xsl:stylesheet>");
    }

    private static InputStream getInputStreamFromUtf8String(String s) {
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }

    public static InputStream xslWithDocAtURI(URI uri) {
        return getInputStreamFromUtf8String("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"3.0\">\n" + "\t<xsl:output method=\"text\"/>\n" + "\t<xsl:template match=\"/\">\n" + "\t\t<xsl:value-of select=\"doc('" + uri.toString() + "')\"/>\n" + "\t</xsl:template>\n" + "</xsl:stylesheet>");
    }

    public static InputStream xslWithParameters() {
        return getInputStreamFromUtf8String(XslWithParameters);
    }

    public static void runServer(String[] args, Runnable fn) {
        runServer(args, () -> {
            fn.run();
            return null;
        });
    }

    public static <T> T runServer(String[] args, Action<T> fn) {
        Server.main(args);
        Spark.awaitInitialization();
        LoggerFactory.getLogger(TestHelpers.class).debug("Started server.");
        T result = fn.run();
        Spark.stop();
        Spark.awaitStop();
        LoggerFactory.getLogger(TestHelpers.class).debug("Stopped server.");
        return result;
    }

    public static <T> T runServer(Action<T> fn) {
        return runServer(new String[]{}, fn);
    }

    public static void runServer(Runnable fn) {
        runServer(new String[]{}, fn);
    }

    public static Response request(String xml, String xsl) {
        return new TestRequest().addXML(xml)
                .addXSL(xsl)
                .execute();
    }
}


