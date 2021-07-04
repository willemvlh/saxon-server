package tv.mediagenix.transformer.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalControllerExceptionHandlerTest {
    @Test
    void handleServerError() {
        Exception e = new RuntimeException("error");
        GlobalControllerExceptionHandler handler = new GlobalControllerExceptionHandler();
        ErrorMessage msg = handler.handleServerError(e);
        assertEquals("error", msg.getMessage());
        assertEquals(500, msg.getStatusCode());
        assertEquals("RuntimeException", msg.getExceptionType());
    }

}