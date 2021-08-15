package io.github.willemvlh.transformer.saxon.json;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmArray;
import net.sf.saxon.s9api.XdmValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonToXdmTransformerTest {

    @Test
    void transform() throws SaxonApiException {
        JsonToXdmTransformer tf = new JsonToXdmTransformer();
        XdmValue val = tf.transform("[]", new Processor(false));
        assertTrue(val instanceof XdmArray);
    }
}