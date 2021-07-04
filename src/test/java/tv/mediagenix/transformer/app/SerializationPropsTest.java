package tv.mediagenix.transformer.app;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tv.mediagenix.transformer.saxon.SerializationProps;

class SerializationPropsTest {
    @Test
    void contentType() {
        SerializationProps sb = new SerializationProps("application/xml", "utf-8");
        Assertions.assertEquals("application/xml;charset=utf-8", sb.getContentType());
    }
}