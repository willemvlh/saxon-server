package tv.mediagenix.transformer.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPart;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(args = {"--port", "3000"})
@AutoConfigureMockMvc
class TransformerTests extends TestClass {

    @Test
    void contextLoads() {
        assertNotNull(controller);
    }

    @Test
    void badURL() throws Exception {
        mvc.perform(post("/abc")).andExpect(status().isNotFound());
    }

    @Test
    void badMethod() throws Exception {
        mvc.perform(get("/query"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").value("Request method 'GET' not supported"));
    }

    @Test
    void simpleTransformation() throws Exception {
        transformDummy("test-1.xsl")
                .andExpect(status().isOk())
                .andExpect(content().string("abc"));
    }

    @Test
    void transformation_iso8859() throws Exception {
        transformDummy("test-iso-8859-15.xsl")
                .andExpect(status().isOk())
                .andExpect(content().bytes("é".getBytes(StandardCharsets.ISO_8859_1)));
    }

    @Test
    void transformation_gzip() throws Exception {
        MockPart xmlPart = xmlPart("dummy.xml.gz");
        MockPart xslPart = xslPart("test-1.xsl.gz");
        xmlPart.getHeaders().setContentType(MediaType.valueOf("application/gzip"));
        xslPart.getHeaders().setContentType(MediaType.valueOf("application/gzip"));

        mvc.perform(
                multipart("/transform")
                        .part(xmlPart)
                        .part(xslPart))
                .andExpect(status().isOk())
               .andExpect(content().string("abc"));
    }

    @Test
    void transformationCompiled() throws Exception {
        transformDummy("test-1.xsl")
                .andExpect(status().isOk())
                .andExpect(content().string("abc"));
    }

    @Test
    void syntaxError() throws Exception {
        transformWithError("dummy.xml", "test-syntax-error.xsl", msg -> {
            assertEquals(400, msg.getStatusCode());
            assertNotEquals("", msg.getMessage());
            assertTrue(msg.getMessage().contains("Element type \"xsl:template\" must be followed by either attribute specifications"));
        });
    }

    @Test
    void compilationError() throws Exception {
        transformWithError("dummy.xml", "test-compilation-error.xsl", msg -> {
            assertEquals(400, msg.getStatusCode());
            assertNotEquals("", msg.getMessage());
            assertTrue(msg.getMessage().toLowerCase().contains("compilation error"));
        });
    }

    @Test
    void compilationErrorNoLocation() throws Exception {
        mvc.perform(multipart("/transform")
                .part(xmlPart("dummy.xml"))
                .part(new MockPart("xsl", "<xsl:template xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"/>".getBytes()))
        ).andExpect(result -> {
            ErrorMessage msg = deserializeError(result.getResponse().getContentAsString());
            validateErrorMessage(msg);
        });
    }

    @Test
    void noBody() throws Exception {
        mvc.perform(multipart("/transform"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No XSL supplied"));
    }

    @Test
    void noStylesheet() throws Exception {
        transformWithError("dummy.xml", "text-file.txt", null);
    }

    @Test
    void transformTerminatedWithMessage() throws Exception {
        transformWithError("dummy.xml", "test-message.xsl", err -> assertEquals("terminated", err.getMessage()));
    }

    @Test
    void transformWithInitialTemplate() throws Exception {
        transform("test-initial-template.xsl")
                .andExpect(status().isOk())
                .andExpect(content().string("hello!"));
    }

    @Test
    void transformWithoutInitialTemplateNoXml() throws Exception {
        mvc.perform(multipart("/transform")
                .part(xslPart("test-1.xsl"))
        ).andDo(result -> {
            MockHttpServletResponse res = result.getResponse();
            assertEquals(400, res.getStatus());
            ErrorMessage msg = deserializeError(res.getContentAsString());
            validateErrorMessage(msg);
        });
    }

    @Test
    void serializationParameters() throws Exception {
        mvc.perform(multipart("/transform")
                .part(new MockPart("xsl", ("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"3.0\">\n" +
                        "   <xsl:template name=\"xsl:initial-template\"><xsl:sequence select=\"map{'a': 'b'}\"/></xsl:template>\n" +
                        "</xsl:stylesheet>").getBytes()))
                .param("output", "method=json;media-type=application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.a").value("b"));
    }

    @Test
    void parameters() throws Exception {
        mvc.perform(multipart("/transform")
                .part(new MockPart("xsl", ("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"3.0\">\n" +
                        "    <xsl:output method=\"text\"/>   \n" +
                        "    <xsl:param name=\"myParam\"/>\n" +
                        "    <xsl:template name=\"xsl:initial-template\">\n" +
                        "      <xsl:value-of select=\"$myParam\"/>\n" +
                        "   </xsl:template>\n" +
                        "</xsl:stylesheet>\n").getBytes()))
                .param("parameters", "myParam=someText;otherParam=bla"))
                .andExpect(status().isOk())
                .andExpect(content().string("someText"));
    }

    @Test
    void jsonAsInput() throws Exception {
        mvc.perform(multipart("/transform").part(
                new MockPart("xsl", ("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"3.0\">\n" +
                        "   <xsl:output media-type=\"application/json\"  method=\"text\"/>\n" +
                        "   <xsl:template match=\"/\"><xsl:sequence select=\"xml-to-json(.)\"/></xsl:template>\n" +
                        "</xsl:stylesheet>\n").getBytes())
        ).part(new MockPart("xml", "{\"a\": \"b\"}".getBytes())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.a").value("b"));
    }

    @Test
    void globalContextVariable() throws Exception {
        transformDummy("test-global-variable.xsl")
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void noContent() throws Exception {
        mvc.perform(multipart("/transform")).andExpect(status().isBadRequest());
    }



}