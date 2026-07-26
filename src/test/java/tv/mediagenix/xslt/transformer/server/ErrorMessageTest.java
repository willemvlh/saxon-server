package tv.mediagenix.xslt.transformer.server;

import org.junit.jupiter.api.Test;
import tv.mediagenix.xslt.transformer.TestHelpers;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonTransformerBuilder;
import tv.mediagenix.xslt.transformer.saxon.core.TransformationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ErrorMessageTest {

    private static final String EXPECTED_JSON =
        "{\"statusCode\":400,\"exceptionType\":\"TransformationException\"," +
        "\"message\":\"net.sf.saxon.s9api.SaxonApiException: Invalid JSON input on line 1: Unexpected symbol: noXml\"}";

    @Test
    public void malformedXmlErrorPayload() throws Exception {
      //Assert that error message structure doesn't change
        try {
            new SaxonTransformerBuilder().build().act(
                new ByteArrayInputStream(TestHelpers.MalformedXml.getBytes(StandardCharsets.UTF_8)),
                TestHelpers.WellFormedXslStream(),
                new ByteArrayOutputStream()
            );
            fail("Expected TransformationException for malformed XML");
        } catch (TransformationException e) {
            String json = new JsonTransformer().render(new ErrorMessage(e, 400));
            assertEquals(EXPECTED_JSON, json);
        }
    }
}
