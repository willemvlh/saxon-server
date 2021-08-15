package io.github.willemvlh.transformer.saxon.json;

import net.sf.saxon.s9api.*;

public class JsonToXmlTransformer implements JsonTransformer {

    public XdmValue transform(String jsonString, Processor processor) throws SaxonApiException {
        XdmFunctionItem fn = XdmFunctionItem.getSystemFunction(processor, new QName("http://www.w3.org/2005/xpath-functions", "json-to-xml"), 1);
        XdmValue result = fn.call(processor, new XdmAtomicValue(jsonString));
        return result.stream().asNode();
    }
}
