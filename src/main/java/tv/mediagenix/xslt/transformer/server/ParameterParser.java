package tv.mediagenix.xslt.transformer.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ParameterParser {

  private Map<String, String> params = new HashMap<>();
  private StringBuilder buffer = new StringBuilder();

  public Map<String, String> parseString(String paramString) throws IllegalArgumentException {
    if (paramString == null || paramString.isEmpty()) {
      throw new IllegalArgumentException("paramString cannot be null or empty");
    }
    var reader = new StringReader(paramString);
    Map.Entry<String, String> param = null;
    try {
      while ((param = readParameter(reader)) != null) {
        params.put(param.getKey(), param.getValue());
      }
      return params;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private Map.Entry<String, String> readParameter(StringReader reader) throws IOException {
    String paramName = "";
    String paramValue = "";
    int counter = 0;
    char c;
    while (true) {
      checkParameterLength(++counter);
      c = (char) reader.read();
      switch (c) {
        case '\\':
          buffer.append(readEscapedCharacter(reader));
          break;
        case '=':
          ensureBufferNotEmpty("Parameter name must not be empty");
          paramName = readAndFlushBuffer();
          break;
        case 0xffff:
          if (paramName.length() == 0) {
            if (buffer.length() > 0) {
              throw new IllegalArgumentException("Unexpected EOF");
            }
            // end of input, no more parameters to read
            return null;
          }
        case ';':
          ensureBufferNotEmpty("Parameter value must not be empty");
          paramValue = readAndFlushBuffer();
          return Map.entry(paramName, paramValue);

        default:
          buffer.append((char) c);
      }
    }
  }

  private String readAndFlushBuffer() {
    String s = buffer.toString();
    resetBuffer();
    return s;
  }

  private void ensureBufferNotEmpty(String errorMsg) {
    if (buffer.length() == 0) {
      throw new IllegalArgumentException(errorMsg);
    }
  }

  private void checkParameterLength(int counter) {
    if (counter > 10000) {
      throw new IllegalArgumentException("Parameter too large");
    }
  }

  private void resetBuffer() {
    buffer = new StringBuilder();
  }

  private Object readEscapedCharacter(StringReader reader) throws IOException {
    char c = (char) reader.read();
    if (c == -1) {
      throw new IllegalArgumentException("Expected character after \\");
    }
    if (c != ';' && c != '=' && c != '\\') {
      throw new IllegalArgumentException("\\" + c + " is an invalid escape sequence");
    }
    return c;
  }

  public Map<String, String> parseStream(InputStream s, int size) throws IOException {
    byte[] bytes = new byte[size];
    int offset = 0;
    int b = s.read();
    while (b != -1) {
      bytes[offset] = (byte) b;
      offset++;
      b = s.read();
    }
    return parseString(new String(bytes, StandardCharsets.UTF_8));
  }
}
