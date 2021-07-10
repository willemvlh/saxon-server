package tv.mediagenix.transformer.app;

import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.Part;

@RequestScope
public class RequestContext {
    private final Part xml;
    private final Part xsl;

    public RequestContext(Part xml, Part xsl) {
        this.xml = xml;
        this.xsl = xsl;
    }

    public Part getXsl() {
        return xsl;
    }

    public Part getXml() {
        return xml;
    }


}
