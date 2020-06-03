package tv.mediagenix.xslt.transformer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface XsltTransformer {
    SerializationProperties transform(InputStream input, InputStream stylesheet, OutputStream output) throws TransformationException, IOException;

}

