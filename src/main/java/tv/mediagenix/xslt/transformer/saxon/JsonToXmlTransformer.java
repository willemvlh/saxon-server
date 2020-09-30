package tv.mediagenix.xslt.transformer.saxon;

import net.sf.saxon.s9api.*;

public class JsonToXmlTransformer {

    public XdmNode transform(String jsonString, Processor processor) throws SaxonApiException {
        XQueryExecutable exec = processor.newXQueryCompiler().compile("json-to-xml(.)");
        XQueryEvaluator eval = exec.load();
        eval.setContextItem(new XdmAtomicValue(jsonString));
        XdmDestination destination = new XdmDestination();
        eval.run(destination);
        return destination.getXdmNode();
    }
}
