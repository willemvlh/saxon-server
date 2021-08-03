package io.github.willemvlh.morph.saxon;

import net.sf.saxon.s9api.*;

public class JsonToXmlTransformer {

    public XdmNode transform(String jsonString, Processor processor) throws SaxonApiException {
        XdmFunctionItem fn = XdmFunctionItem.getSystemFunction(processor, new QName("http://www.w3.org/2005/xpath-functions", "json-to-xml"), 1);
        XdmValue result = fn.call(processor, new XdmAtomicValue(jsonString));
        return result.stream().asNode();
    }
}
