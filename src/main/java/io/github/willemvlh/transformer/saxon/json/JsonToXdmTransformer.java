package io.github.willemvlh.transformer.saxon.json;

import net.sf.saxon.s9api.*;

public class JsonToXdmTransformer implements JsonTransformer {
    @Override
    public XdmValue transform(String jsonString, Processor processor) throws SaxonApiException {
        XdmFunctionItem fn = XdmFunctionItem.getSystemFunction(processor, new QName("http://www.w3.org/2005/xpath-functions", "parse-json"), 1);
        return fn.call(processor, XdmValue.makeValue(jsonString));
    }
}
