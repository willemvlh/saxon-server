package io.github.willemvlh.morph.app;

import io.github.willemvlh.morph.saxon.TransformationException;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockPart;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

class GlobalControllerExceptionHandlerTest {

    @Test
    void handleServerError() {
        Exception e = new RuntimeException("error");
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(this.getClass());
        GlobalControllerExceptionHandler handler = new GlobalControllerExceptionHandler(logger);
        ErrorMessage msg = handler.handleServerError(e);
        assertEquals("error", msg.getMessage());
        assertEquals(500, msg.getStatusCode());
        assertEquals("RuntimeException", msg.getExceptionType());
    }

    @Test
    void errorLogging() {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("test");
        TestAppender app = (TestAppender) logger.getAppender("STDOUT");
        GlobalControllerExceptionHandler handler = new GlobalControllerExceptionHandler(logger);
        HttpServletRequest req = multipart("/transform").part(
                new MockPart("xml", "<test/>".getBytes(StandardCharsets.UTF_8))
        ).part(new MockPart("xsl", "bla".getBytes(StandardCharsets.UTF_8))).buildRequest(new MockServletContext());
        handler.handleBadRequest(req, new TransformationException("abc"));
        assertEquals(app.getEvents().size(), 1);
        String expectedMsg = "Transformation exception: abc\n" +
                "Received following input: \n" +
                "xml: \n" +
                "<test/>\n" +
                "\n" +
                "xsl: \n" +
                "bla";
        assertEquals(expectedMsg.trim(), app.getEvents().get(0).getMessage().trim());

    }


}