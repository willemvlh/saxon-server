import XsltTransformer.*;
import net.sf.saxon.s9api.SaxonApiException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import spark.utils.Assert;

import java.io.*;

public class SaxonTransformerTest {
    SaxonTransformer tf = new SaxonTransformer();

    @Test
    public void transformTest() throws UnsupportedEncodingException, SaxonApiException {
        String xml = "<test/>";
        String xsl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"3.0\">\n" +
                "    <xsl:output method=\"text\"/>\n" +
                "    <xsl:template match=\"/\">\n" +
                "        <xsl:text>hello</xsl:text>\n" +
                "    </xsl:template>\n" +
                "</xsl:stylesheet>";
        ByteArrayOutputStream output = transformWithStrings(xml, xsl);
        Assert.isTrue(output.toString("utf-8").equals("hello"), "The output should be 'hello'");

    }

    @Test
    public void malformedXslTest(){

        Assertions.assertThrows(SaxonApiException.class, () -> transformWithStrings("bad xml", "bad xsl"), "Malformed input should trigger an exception");
    }

    private ByteArrayOutputStream transformWithStrings(String xml, String xsl) throws UnsupportedEncodingException, SaxonApiException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream input = new ByteArrayInputStream(xml.getBytes("utf-8"));
        InputStream xslStr = new ByteArrayInputStream(xsl.getBytes("utf-8"));
        tf.transform(input, xslStr, output);
        return output;
    }
}
