package io.github.willemvlh.transformer.saxon;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.junit.jupiter.api.Test;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonToXmlTransformerTest {

    @Test
    void transform() throws TransformerException, SaxonApiException {
        JsonToXmlTransformer tf = new JsonToXmlTransformer();
        XdmNode resultNode = tf.transform("{}", new Processor(false));
        DOMResult res = new DOMResult();
        Transformer xf = TransformerFactory.newInstance().newTransformer();
        Source resultSource = resultNode.asSource();
        xf.transform(resultSource, res);
        assertEquals("map", res.getNode().getFirstChild().getLocalName());
        assertThrows(SaxonApiException.class, () -> tf.transform("x", new Processor(false)));
    }
}