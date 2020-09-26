package tv.mediagenix.xslt.transformer.saxon.actors;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.serialize.SerializationProperties;
import tv.mediagenix.xslt.transformer.saxon.SerializationProps;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class SaxonXQueryPerformer extends SaxonActor {
    private XQueryExecutable executable;

    public SaxonXQueryPerformer(boolean insecure) {
        super(insecure);
    }

    public SaxonXQueryPerformer() {
    }

    public SaxonXQueryPerformer(File config) throws TransformationException {
        super(config);
    }

    @Override
    public SerializationProps act(InputStream is, InputStream query, OutputStream output) throws TransformationException {
        try {
            XQueryEvaluator e = newEvaluatorOnQuery(query);
            e.setSource(newSAXSource(is));
            return evaluate(e, output);
        } catch (SaxonApiException e) {
            throw new TransformationException(e);
        }
    }

    @Override
    public SerializationProps act(InputStream query, OutputStream output) throws TransformationException {
        try {
            XQueryEvaluator e = newEvaluatorOnQuery(query);
            return evaluate(e, output);
        } catch (SaxonApiException saxonApiException) {
            throw new TransformationException(saxonApiException);
        }
    }

    private XQueryEvaluator newEvaluatorOnQuery(InputStream query) throws SaxonApiException {
        this.executable = this.getProcessor().newXQueryCompiler().compile(query);
        return this.executable.load();
    }

    @Override
    protected SerializationProps getSerializationProperties(Serializer s) {
        SerializationProperties props = this.executable.getUnderlyingCompiledQuery().getExecutable().getPrimarySerializationProperties();
        props = props.combineWith(s.getSerializationProperties());
        return new SerializationProps(props.getProperty("media-type"), props.getProperty("encoding"));
    }

    private SerializationProps evaluate(XQueryEvaluator e, OutputStream output) throws SaxonApiException {
        Serializer s = newSerializer(output);
        e.setDestination(s);
        e.run();
        return getSerializationProperties(s);
    }

}
