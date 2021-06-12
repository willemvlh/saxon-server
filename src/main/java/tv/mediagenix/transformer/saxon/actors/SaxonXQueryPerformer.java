package tv.mediagenix.transformer.saxon.actors;

import net.sf.saxon.s9api.*;
import net.sf.saxon.serialize.SerializationProperties;
import tv.mediagenix.transformer.saxon.SerializationProps;
import tv.mediagenix.transformer.saxon.TransformationException;

import java.io.InputStream;
import java.io.OutputStream;

public class SaxonXQueryPerformer extends SaxonActor {
    private XQueryExecutable executable;

    private XQueryEvaluator newEvaluatorOnQuery(InputStream query) throws SaxonApiException {
        this.executable = this.getProcessor().newXQueryCompiler().compile(query);
        XQueryEvaluator evaluator = this.executable.load();
        this.getParameters().forEach(evaluator::setExternalVariable);
        return evaluator;
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
