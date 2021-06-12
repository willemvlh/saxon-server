package tv.mediagenix.transformer.server;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ParameterParserTest {
    @Test
    public void testParse() {
        ParameterParser p = new ParameterParser();
        Map<String, String> result = p.parseString("koe=kalf;varken=big");
        assertEquals("kalf", result.get("koe"));
        assertEquals("big", result.get("varken"));
        assertEquals(2, result.size());
    }

    @Test
    public void testParseEscape() {
        ParameterParser p = new ParameterParser();
        Map<String, String> result = p.parseString("param=nee\\;soms");
        assertEquals(1, result.size());
        assertEquals("nee;soms", result.get("param"));
        assertFalse(result.containsKey("soms"));
    }

    @Test
    public void testParseNoEscape() {
        ParameterParser p = new ParameterParser();
        Map<String, String> result = p.parseString("param=nee\\soms");
        assertEquals(1, result.size());
        assertEquals("nee\\soms", result.get("param"));
    }

    @Test
    public void testParseWithEqualsSign() {
        ParameterParser p = new ParameterParser();
        assertEquals("1=2", p.parseString("input=1=2").get("input"));
    }

    @Test
    public void testStream() throws IOException {
        ParameterParser p = new ParameterParser();
        String s = "koe=kalf;varken=big";
        Map<String, String> result = p.parseStream(new ByteArrayInputStream(s.getBytes()), s.length());
        assertEquals("kalf", result.get("koe"));
        assertEquals("big", result.get("varken"));
        assertEquals(2, result.size());
    }

}