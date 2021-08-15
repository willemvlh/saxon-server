package io.github.willemvlh.transformer.saxon.json;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmValue;

public interface JsonTransformer {
    XdmValue transform(String jsonString, Processor processor) throws SaxonApiException;
}
