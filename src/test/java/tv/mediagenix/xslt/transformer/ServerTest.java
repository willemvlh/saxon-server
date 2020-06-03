package tv.mediagenix.xslt.transformer;

import com.google.gson.Gson;
import org.eclipse.jetty.util.MultiPartInputStreamParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import spark.Request;
import spark.Response;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServerTest {

    @Mock Request req;
    @Mock Response res;
    @Mock HttpServletRequest raw;
    @Mock HttpServletResponse raw_;
    @Mock MultiPartInputStreamParser.MultiPart xmlPart;
    @Mock MultiPartInputStreamParser.MultiPart xslPart;

    private Server server;

    private final int PORT = 4938;

    private ServletOutputStream outputStreamForTest = new ServletOutputStream() {
        private StringBuilder builder = new StringBuilder();
        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) { }

        @Override
        public void write(int b) {
                builder.append((char) b);
        }

        public String toString(){
            return builder.toString();
        }
    };

    @BeforeEach
    void before() throws IOException, ServletException{
        ServerOptions opts = new ServerOptions();
        opts.setPort(PORT);
        server = Server.newServer(opts);
        MockitoAnnotations.initMocks(this);
        when(req.raw()).thenReturn(raw);
        when(raw.getPart("xml")).thenReturn(xmlPart);
        when(raw.getPart("xsl")).thenReturn(xslPart);
        when(res.raw()).thenReturn(raw_);
        when(res.raw().getOutputStream()).thenReturn(outputStreamForTest);
        when(xmlPart.getInputStream()).thenReturn(TestHelpers.WellFormedXmlStream());
        when(xslPart.getInputStream()).thenReturn(TestHelpers.WellFormedXslStream());
    }

    @AfterEach
    void after() throws InterruptedException {
        server.stop();
        Thread.sleep(250);
    }

    @Test
    void testCorrectRequest() throws IOException {
        Response result = (Response) server.handleRequest(req, res);
        Assertions.assertEquals("hello", result.raw().getOutputStream().toString());
    }

    @Test
    void testIncorrectXslRequest() throws IOException{
        when(xslPart.getInputStream()).thenReturn(TestHelpers.IncorrectXslStream());
        server.handleRequest(req, res);
        verify(res).status(400);
        verify(res).type("application/json");
        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(res).body(arg.capture());
        verifyErrorMessageFromJsonString(arg.getValue());
    }

    @Test
    void testIncorrectXmlRequest() throws IOException{
        when(xmlPart.getInputStream()).thenReturn(TestHelpers.MalformedXmlStream());
        server.handleRequest(req, res);
        verify(res).status(400);
        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(res).body(arg.capture());
        verifyErrorMessageFromJsonString(arg.getValue());
    }

    @Test
    void testPartWithNoStream() throws IOException {
        when(xmlPart.getInputStream()).thenReturn(null);
        server.handleRequest(req, res);
        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(res).status(400);
    }

    @Test
    void testRequestWithPartNull() throws IOException, ServletException {
        when(req.raw().getPart("xml")).thenReturn(null);
        server.handleRequest(req,res);
        verify(res).status(400);
        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(res).body(arg.capture());
        ErrorMessage msg = captureError(arg.getValue());
        Assertions.assertEquals(InvalidRequestException.class.getSimpleName(), msg.exceptionType);
    }

    @Test
    void testErrorGeneratedByXslMessage() throws IOException {
        when(xslPart.getInputStream()).thenReturn(TestHelpers.MessageInvokingXslStream());
        when(xmlPart.getInputStream()).thenReturn(TestHelpers.WellFormedXmlStream());
        server.handleRequest(req, res);
        verify(res).status(400);
        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(res).body(arg.capture());
        ErrorMessage error = captureError(arg.getValue());
        Assertions.assertEquals(TestHelpers.message, error.message);
    }

    @Test
    void generalExceptionTest() throws IOException{
        when(xmlPart.getInputStream()).thenThrow(IllegalArgumentException.class);
        server.handleRequest(req,res);
        verify(res).status(500);
    }

    private void verifyErrorMessageFromJsonString(String str){
        ErrorMessage msg = captureError(str);
        Assertions.assertNotNull(msg.message);
        Assertions.assertNotNull(msg.exceptionType);
        Assertions.assertTrue(msg.statusCode >= 400);
    }

    private ErrorMessage captureError(String str){
        Gson g = new Gson();
        return g.fromJson(str, ErrorMessage.class);
    }
}
