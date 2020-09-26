package tv.mediagenix.xslt.transformer.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class ParameterParser {

    public Map<String, String> parseString(String paramString) {
        HashMap<String, String> params = new HashMap<>();
        for (String paramP : paramString.split(";")) {
            String[] splits = paramP.split("=");
            if (splits.length == 2) {
                params.put(splits[0], splits[1]);
            }
        }
        return params;
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
        return parseString(new String(bytes, Charset.defaultCharset()));
    }
}
