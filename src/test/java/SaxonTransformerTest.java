import XsltTransformer.*;
import net.sf.saxon.s9api.SaxonApiException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import spark.utils.Assert;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class SaxonTransformerTest {
    SaxonTransformer tf = new SaxonTransformer();

    @Test
    public void transformTest() throws UnsupportedEncodingException, TransformationException {
        ByteArrayOutputStream output = transformWithStrings(TestHelpers.WellFormedXml, TestHelpers.WellFormedXsl);
        Assert.isTrue(output.toString("utf-8").equals("hello"), "The output should be 'hello'");
    }

    @Test
    public void malformedXslTest(){

        Assertions.assertThrows(TransformationException.class, () -> transformWithStrings(TestHelpers.MalformedXml, TestHelpers.WellFormedXsl), "Malformed input should trigger an exception");
    }

    @Test
    public void errorMsgTest() throws UnsupportedEncodingException {
        try{
            transformWithStrings("bad xml", "bad xsl");
        }
        catch (TransformationException e){
            Assertions.assertTrue(e.getMessage().contains("a"));
        }
    }

    @Test
    public void errorListTest(){
        try{
            transformWithStrings("<root/>", "<badXSl/>");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (TransformationException e) {
            Assertions.assertFalse(tf.getErrorList().isEmpty());
        }
    }

    private ByteArrayOutputStream transformWithStrings(String xml, String xsl) throws UnsupportedEncodingException, TransformationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream input = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        InputStream xslStr = new ByteArrayInputStream(xsl.getBytes(StandardCharsets.UTF_8));
        tf.transform(input, xslStr, output);
        return output;
    }
}
