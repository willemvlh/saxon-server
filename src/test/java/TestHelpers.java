import jdk.internal.util.xml.impl.Input;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TestHelpers {
    private static String WellFormedXml = "<root/>";
    private static String WellFormedXsl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"3.0\">\n" +
            "    <xsl:output method=\"text\"/>\n" +
            "    <xsl:template match=\"/\">\n" +
            "        <xsl:text>hello</xsl:text>\n" +
            "    </xsl:template>\n" +
            "</xsl:stylesheet>";
    public static String MalformedXml = "noXml";
    public static String IncorrectXsl = WellFormedXsl.replaceAll("stylesheet", "wrongTag");
    public static InputStream WellFormedXmlStream = getInputStreamFromUtf8String(WellFormedXml);
    public static InputStream WellFormedXslStream = getInputStreamFromUtf8String(WellFormedXsl);
    public static InputStream MalformedXmlStream = getInputStreamFromUtf8String(MalformedXml);
    public static InputStream IncorrectXslStream = getInputStreamFromUtf8String(IncorrectXsl);

    private static InputStream getInputStreamFromUtf8String(String s) {
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }
}
