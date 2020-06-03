package tv.mediagenix.xslt.transformer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SerializationPropertiesTest {
    @Test
    void ContentTypeTest(){
        SerializationProperties sb = new SerializationProperties("application/xml", "utf-8");
        Assertions.assertEquals("application/xml;charset=utf-8", sb.contentType());
    }
}