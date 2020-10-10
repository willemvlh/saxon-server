package tv.mediagenix.xslt.transformer.saxon.actors;

import net.sf.saxon.s9api.*;
import net.sf.saxon.serialize.SerializationProperties;
import tv.mediagenix.xslt.transformer.saxon.SerializationProps;
import tv.mediagenix.xslt.transformer.saxon.TransformationException;

import java.io.InputStream;
import java.io.OutputStream;

public class SaxonXQueryPerformer extends SaxonActor {
    private XQueryExecutable executable;

    private XQueryEvaluator newEvaluatorOnQuery(InputStream query) throws SaxonApiException {
        this.executable = this.getProcessor().newXQueryCompiler().compile(query);
        return this.executable.load();
    }

    @Override
    public SerializationProps act(XdmValue input, InputStream query, OutputStream output) throws TransformationException {
        try {
            XQueryEvaluator e = newEvaluatorOnQuery(query);
            if (!input.isEmpty()) {
                e.setContextItem(input.itemAt(0));
            }
            return evaluate(e, output);
        } catch (SaxonApiException e) {
            throw new TransformationException(e.getMessage(), e);
        }
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
