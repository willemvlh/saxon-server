package io.github.willemvlh.transformer.app;

import io.github.willemvlh.transformer.saxon.SerializationProps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SerializationPropsTest {
    @Test
    void contentType() {
        SerializationProps sb = new SerializationProps("application/xml", "utf-8");
        Assertions.assertEquals("application/xml;charset=utf-8", sb.getContentType());
    }
}