package tv.mediagenix.transformer.saxon;

import net.sf.saxon.s9api.MessageListener2;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

import javax.xml.transform.SourceLocator;

public class SaxonMessageListener implements MessageListener2 {
    public String errorString;

    @Override
    public void message(XdmNode xdmNode, QName name, boolean b, SourceLocator sourceLocator) {
        if (b) errorString = xdmNode.toString();
    }
}
