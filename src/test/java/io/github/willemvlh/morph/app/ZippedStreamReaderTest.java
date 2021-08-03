package io.github.willemvlh.morph.app;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZippedStreamReaderTest {
    @Test
    void unzip() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream os = new GZIPOutputStream(bos);
        os.write(new byte[]{65, 66, 67});
        os.close();
        GZIPInputStream is = new GZIPInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ZippedStreamReader reader = new ZippedStreamReader();
        InputStream unzippedStream = reader.unzipStream(is);
        byte[] result = new byte[3];
        for (int i = 0; i < 3; i++) {
            result[i] = (byte) unzippedStream.read();
        }
        assertEquals("ABC", new String(result));


    }
}