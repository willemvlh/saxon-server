package tv.mediagenix.transformer.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.StreamUtils;
import tv.mediagenix.transformer.ErrorMessage;

import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@SpringBootTest(args = {"--port", "10000"})
@AutoConfigureMockMvc
public abstract class TestClass {

    @Autowired
    public TransformController controller;

    @Autowired
    public MockMvc mvc;

    @BeforeAll
    public static void setUp(){
        System.setProperty("javax.xml.xpath.XPathFactory:http://java.sun.com/jaxp/xpath/dom", "net.sf.saxon.xpath.XPathFactoryImpl");
    }

    MockPart xslPart(String fn) throws IOException {
        return new MockPart("xsl", StreamUtils.copyToByteArray(this.getClass().getResourceAsStream(fn)));
    }

    MockPart xmlPart(String fn) throws IOException {
        return new MockPart("xml", StreamUtils.copyToByteArray(this.getClass().getResourceAsStream(fn)));
    }

    ResultActions transform(String xmlFn, String xslFn) throws Exception {
        return mvc.perform(multipart("/transform")
                .part(xmlPart(xmlFn))
                .part(xslPart(xslFn)));
    }

    ResultActions transform(String xslFn) throws Exception {
        return mvc.perform(multipart("/transform")
                .part(xslPart(xslFn)));
    }

    void transformWithError(String xmlFn, String xslFn, Consumer<ErrorMessage> cb) throws Exception {
        transform(xmlFn, xslFn).andDo(mvcResult -> {
            MockHttpServletResponse res = mvcResult.getResponse();
            assertEquals(400, res.getStatus());
            ErrorMessage msg = deserializeError(res.getContentAsString());
            validateErrorMessage(msg);
            if(cb != null) cb.accept(msg);
        });
    }

    void validateErrorMessage(ErrorMessage msg){
        assertNotNull(msg.getMessage());
        assertFalse(msg.getMessage().isEmpty());
        assertTrue(msg.getStatusCode() >= 200 && msg.getStatusCode() < 500);
        assertNotNull(msg.getExceptionType());
        assertFalse(msg.getExceptionType().isEmpty());
    }

    ResultActions transformDummy(String xslFn) throws Exception{
        return transform("dummy.xml", xslFn);
    }

    ErrorMessage deserializeError(String error) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(error, ErrorMessage.class);
    }
}
