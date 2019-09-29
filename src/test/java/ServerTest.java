import XsltTransformer.*;
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
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

import static org.mockito.Mockito.*;

public class ServerTest {

    @Mock Request req;
    @Mock Response res;
    @Mock HttpServletRequest raw;
    @Mock HttpServletResponse raw_;
    @Mock MultiPartInputStreamParser.MultiPart xmlPart;
    @Mock MultiPartInputStreamParser.MultiPart xslPart;

    Server server;

    ServletOutputStream outputStreamForTest = new ServletOutputStream() {
        private StringBuilder builder = new StringBuilder();
        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) { }

        @Override
        public void write(int b) throws IOException {
                builder.append((char) b);
        }

        public String toString(){
            return builder.toString();
        }
    };

    @BeforeEach
    public void before() throws IOException, ServletException{
        server = new Server();
        server.configureRoutes();
        MockitoAnnotations.initMocks(this);
        when(req.raw()).thenReturn(raw);
        when(raw.getPart("xml")).thenReturn(xmlPart);
        when(raw.getPart("xsl")).thenReturn(xslPart);
        when(res.raw()).thenReturn(raw_);
        when(res.raw().getOutputStream()).thenReturn(outputStreamForTest);
        when(xmlPart.getInputStream()).thenReturn(TestHelpers.WellFormedXmlStream);
        when(xslPart.getInputStream()).thenReturn(TestHelpers.WellFormedXslStream);
    }

    @AfterEach
    public void after() throws InterruptedException {
        server.stop();
        Thread.sleep(500);
    }

    @Test
    public void testCorrectRequest() throws IOException {
        Response result = (Response) server.handleRequest(req, res);
        Assertions.assertEquals("hello", result.raw().getOutputStream().toString());
    }

    @Test
    public void testIncorrectXslRequest() throws IOException{
        when(xslPart.getInputStream()).thenReturn(TestHelpers.IncorrectXslStream);
        server.handleRequest(req, res);
        verify(res).status(400);
        verify(res).type("application/json");
        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(res).body(arg.capture());
        verifyErrorMessageFromJsonString(arg.getValue());
    }

    @Test
    public void testIncorrectXmlRequest() throws IOException{
        when(xmlPart.getInputStream()).thenReturn(TestHelpers.MalformedXmlStream);
        server.handleRequest(req, res);
        verify(res).status(400);
        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(res).body(arg.capture());
        verifyErrorMessageFromJsonString(arg.getValue());
    }

    private void verifyErrorMessageFromJsonString(String str){
        Gson g = new Gson();
        ErrorMessage msg = g.fromJson(str, ErrorMessage.class);
        Assertions.assertNotNull(msg.message);
        Assertions.assertNotNull(msg.exceptionType);
        Assertions.assertNotNull(msg.statusCode);
    }
}
