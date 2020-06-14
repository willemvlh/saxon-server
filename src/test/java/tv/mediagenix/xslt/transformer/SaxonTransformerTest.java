package tv.mediagenix.xslt.transformer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import spark.utils.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class SaxonTransformerTest {
    SaxonTransformer tf = new SaxonTransformer();

    @Test
    public void transformTest() throws UnsupportedEncodingException, TransformationException {
        ByteArrayOutputStream output = transformWithStrings(TestHelpers.WellFormedXml, TestHelpers.WellFormedXsl);
        Assert.isTrue(output.toString("utf-8").equals("hello"), "The output should be 'hello'");
    }

    @Test
    public void malformedXslTest() {

        Assertions.assertThrows(TransformationException.class, () -> transformWithStrings(TestHelpers.MalformedXml, TestHelpers.WellFormedXsl), "Malformed input should trigger an exception");
    }

    @Test
    public void errorMsgTest() {
        try {
            transformWithStrings("bad xml", "bad xsl");
        } catch (TransformationException e) {
            Assertions.assertTrue(e.getMessage().contains("a"));
        }
    }

    @Test
    public void messageTest() {
        try {
            transformWithStrings("<x/>", TestHelpers.MessageInvokingXsl);
            Assertions.fail("should have thrown");
        } catch (TransformationException e) {
            Assertions.assertEquals(e.getMessage(), TestHelpers.message);
        }

    }

    @Test
    public void errorListTest() {
        try {
            transformWithStrings("<root/>", "<badXSl/>");
        } catch (TransformationException e) {
            Assertions.assertFalse(tf.getErrorList().isEmpty());
        }
    }

    private ByteArrayOutputStream transformWithStrings(String xml, String xsl) throws TransformationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream input = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        InputStream xslStr = new ByteArrayInputStream(xsl.getBytes(StandardCharsets.UTF_8));
        tf.transform(input, xslStr, output);
        return output;
    }
}