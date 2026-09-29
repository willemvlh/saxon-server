package tv.mediagenix.xslt.transformer.server;

import org.junit.jupiter.api.Test;
import tv.mediagenix.xslt.transformer.TestHelpers;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonTransformerBuilder;
import tv.mediagenix.xslt.transformer.saxon.core.TransformationException;
import tv.mediagenix.xslt.transformer.saxon.core.TypedInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErrorMessageTest {

    private static final String EXPECTED_JSON =
        "{\"statusCode\":400,\"exceptionType\":\"TransformationException\"," +
        "\"message\":\"net.sf.saxon.s9api.SaxonApiException: Invalid JSON input on line 1: Unexpected symbol: noXml\"}";

    @Test
    void malformedXmlErrorPayload() throws Exception {
      var input = new ByteArrayInputStream(TestHelpers.MalformedXml.getBytes(StandardCharsets.UTF_8));
      var malformedStream = new TypedInputStream(input, "application/json");
      //Assert that error message structure doesn't change
      TransformationException e = assertThrows(TransformationException.class,
          () -> new SaxonTransformerBuilder().build().act(
              malformedStream,
              TestHelpers.WellFormedXslStream(),
              new ByteArrayOutputStream()
          ), "Expected TransformationException for malformed XML");
      String json = new JsonTransformer().render(new ErrorMessage(e, 400));
      assertEquals(EXPECTED_JSON, json);
    }
}
