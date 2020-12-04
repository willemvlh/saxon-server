package tv.mediagenix.xslt.transformer.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ParameterParser {

    public Map<String, String> parseString(String paramString) {
        HashMap<String, String> params = new HashMap<>();
        for (String param : paramString.split("(?<!\\\\);")) { //split on ; except when preceded by \
            String unescaped = unescape(param);
            int separator = unescaped.indexOf('=');
            params.put(unescaped.substring(0, separator), unescaped.substring(separator + 1));
        }
        return params;
    }

    private String unescape(String paramString) {
        return paramString.replace("\\;", ";");
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
