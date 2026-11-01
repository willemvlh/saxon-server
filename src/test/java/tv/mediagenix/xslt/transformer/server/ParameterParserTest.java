package tv.mediagenix.xslt.transformer.server;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParameterParserTest {
  @Test
  public void parseMultiple() {
    ParameterParser p = new ParameterParser();
    Map<String, String> result = p.parseString("koe=kalf;varken=big");
    assertEquals("kalf", result.get("koe"));
    assertEquals("big", result.get("varken"));
    assertEquals(2, result.size());
  }

  @Test
  public void escapeSemiColon() {
    ParameterParser p = new ParameterParser();
    Map<String, String> result = p.parseString("param=nee\\;soms");
    assertEquals(1, result.size());
    assertEquals("nee;soms", result.get("param"));
    assertFalse(result.containsKey("soms"));
  }

  @Test
  public void escapeEqualsSign(){
    ParameterParser p = new ParameterParser();
    Map<String, String> result = p.parseString("a=b\\=");
    assertEquals(1, result.size());
    assertEquals("b=", result.get("a"));
  }

  @Test
  public void invalidEscapeSequence() {
    ParameterParser p = new ParameterParser();
    assertThrows(IllegalArgumentException.class, () -> p.parseString("param=nee\\soms"));
  }

  @Test
  public void stringEndsWithSemiColon() {
    // let's allow it?
    ParameterParser p = new ParameterParser();
    assertEquals("b", p.parseString("a=b;").get("a"));
  }

  @Test
  public void stringWithoutEqualsSign() {
    ParameterParser p = new ParameterParser();
    assertThrows(IllegalArgumentException.class, () -> p.parseString("eh?"));
  }

  @Test
  public void escapedBackslashBeforeSeparator() {
    ParameterParser p = new ParameterParser();
    Map<String, String> result = p.parseString("a=C:\\\\temp\\\\;b=2");
    assertEquals(2, result.size());
    assertEquals("C:\\temp\\", result.get("a"));
    assertEquals("2", result.get("b"));
  }

  @Test
  public void emptyParameterName() {
    ParameterParser p = new ParameterParser();
    assertThrows(IllegalArgumentException.class, () -> p.parseString("=1"));
  }

  @Test
  public void parseStream() throws IOException {
    ParameterParser p = new ParameterParser();
    String s = "koe=kalf;varken=big";
    Map<String, String> result = p.parseStream(new ByteArrayInputStream(s.getBytes()), s.length());
    assertEquals("kalf", result.get("koe"));
    assertEquals("big", result.get("varken"));
    assertEquals(2, result.size());
  }

}
