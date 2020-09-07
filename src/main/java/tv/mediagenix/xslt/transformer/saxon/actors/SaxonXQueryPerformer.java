package tv.mediagenix.xslt.transformer.saxon.actors;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryEvaluator;
import tv.mediagenix.xslt.transformer.saxon.SerializationProperties;
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

    @Override
    public SerializationProperties act(InputStream is, InputStream query, OutputStream output) throws TransformationException {
        try {
            XQueryEvaluator e = newEvaluatorOnQuery(query);
            e.setSource(newSAXSource(is));
            return evaluate(e, output);
        } catch (SaxonApiException e) {
            throw new TransformationException(e);
        }
    }

    @Override
    public SerializationProperties act(InputStream query, OutputStream output) throws TransformationException {
        try {
            XQueryEvaluator e = newEvaluatorOnQuery(query);
            return evaluate(e, output);
        } catch (SaxonApiException saxonApiException) {
            throw new TransformationException(saxonApiException);
        }
    }

    private XQueryEvaluator newEvaluatorOnQuery(InputStream query) throws SaxonApiException {
        return this.getProcessor().newXQueryCompiler().compile(query).load();
    }

    private SerializationProperties evaluate(XQueryEvaluator e, OutputStream output) throws SaxonApiException {
        Serializer s = newSerializer(output);
        e.setDestination(s);
        e.run();
        return new SerializationProperties("application/xml", "utf-8");
    }
}
