import XsltTransformer.SaxonTransformer;
import XsltTransformer.TransformationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URISyntaxException;

public class ServerOptionsTest {
    @Test
    public void ParseTest() throws URISyntaxException, TransformationException {
        File configFile = new File(this.getClass().getResource("saxon-config.xml").toURI());
        SaxonTransformer xf = new SaxonTransformer(configFile);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        xf.transform(TestHelpers.WellFormedXmlStream(), TestHelpers.SystemPropertyInvokingXslStream(), os);
        Assertions.assertNotEquals(os.size(), 0);
    }

    @Test
    public void NoOptionsParseTest() throws TransformationException {
        SaxonTransformer xf = new SaxonTransformer(null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        xf.transform(TestHelpers.WellFormedXmlStream(), TestHelpers.WellFormedXslStream(), os);
        Assertions.assertNotEquals(os.size(), 0);
    }

    @Test
    public void WrongConfigFileTest() {
        SaxonTransformer xf = new SaxonTransformer(new File("unknown"));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Assertions.assertThrows(TransformationException.class, () -> xf.transform(TestHelpers.WellFormedXmlStream(), TestHelpers.WellFormedXslStream(), os));
    }

    @Test
    public void DisallowExternalFunctionTest() throws URISyntaxException, TransformationException {
        //enabling the disallow-external-functions makes it impossible to access java system properties.
        File f = new File(this.getClass().getResource("saxon-config-no-external-fn.xml").toURI());
        SaxonTransformer xf = new SaxonTransformer(f);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        xf.transform(TestHelpers.WellFormedXmlStream(), TestHelpers.SystemPropertyInvokingXslStream(), os);
        Assertions.assertEquals(os.size(), 0);
    }


}

