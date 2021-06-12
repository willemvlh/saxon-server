package tv.mediagenix.transformer.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class ZippedStreamReader {
    public InputStream unzipStream(GZIPInputStream zipStream) throws IOException {
        ByteArrayOutputStream writeStream = new ByteArrayOutputStream();
        int data = zipStream.read();
        while (data != -1) {
            writeStream.write(data);
            data = zipStream.read();
        }
        return new ByteArrayInputStream(writeStream.toByteArray());
    }
}
