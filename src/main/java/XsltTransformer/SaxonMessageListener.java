package XsltTransformer;

import net.sf.saxon.s9api.MessageListener;
import net.sf.saxon.s9api.XdmNode;

import javax.xml.transform.SourceLocator;

public class SaxonMessageListener implements MessageListener {
    public String errorString;

    @Override
    public void message(XdmNode xdmNode, boolean b, SourceLocator sourceLocator) {
        if (b) errorString = xdmNode.toString();
    }
}
