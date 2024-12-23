package tv.mediagenix.xslt.transformer.saxon;

import net.sf.saxon.s9api.MessageListener2;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.SourceLocator;

public class SaxonMessageListener implements MessageListener2 {
    public String errorString;

    @Override
    public void message(XdmNode xdmNode, QName name, boolean b, SourceLocator sourceLocator) {
        if (b) {
            errorString = xdmNode.toString();
        }
        else{
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.info("Message received. Line: {}, column: {}", sourceLocator.getLineNumber(), sourceLocator.getColumnNumber());
            logger.info("Message: {}", xdmNode.toString());
        }
    }
}
