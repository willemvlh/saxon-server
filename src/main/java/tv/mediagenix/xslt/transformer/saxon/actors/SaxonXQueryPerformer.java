package tv.mediagenix.xslt.transformer.saxon.actors;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryEvaluator;
import tv.mediagenix.xslt.transformer.SerializationProperties;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class SaxonXQueryPerformer extends SaxonActor {
    public SaxonXQueryPerformer(boolean insecure) {
        super(insecure);
    }

    public SaxonXQueryPerformer() {
    }

    public SaxonXQueryPerformer(File config) throws TransformationException {
        super(config);
    }

    public SerializationProperties act(InputStream is, InputStream query, OutputStream output) throws TransformationException {
        try {
            XQueryEvaluator e = this.getProcessor().newXQueryCompiler().compile(query).load();
            Serializer s = newSerializer(output);
            e.setDestination(s);
            e.setSource(newSAXSource(is));
            e.run();
            return new SerializationProperties("application/xml", "utf-8");
        } catch (SaxonApiException e) {
            throw new TransformationException(e);
        }
    }
}
