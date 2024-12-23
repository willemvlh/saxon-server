package tv.mediagenix.xslt.transformer;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class LoggerTest {
    @Test
    public void hasLogger() {
        Logger logger = LoggerFactory.getLogger(LoggerTest.class);
        assertEquals(logger.getClass(), ch.qos.logback.classic.Logger.class);
    }
}

