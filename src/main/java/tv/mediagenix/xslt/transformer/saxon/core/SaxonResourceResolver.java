package tv.mediagenix.xslt.transformer.saxon.core;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.lib.ResourceRequest;
import net.sf.saxon.lib.ResourceResolver;
import net.sf.saxon.trans.XPathException;

public class SaxonResourceResolver implements ResourceResolver {

  private Map<String, InputStream> files = Map.of();
  private boolean allowExternalResources;
  private ResourceResolver next;
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public SaxonResourceResolver(ResourceResolver next) {
    this.next = next;
    this.allowExternalResources = false;
  }

  public boolean allowExternalResources() {
    return allowExternalResources;
  }

  public void setAllowExternalResources(boolean allowExternalResources) {
    this.allowExternalResources = allowExternalResources;
  }

  public Map<String, InputStream> getFiles() {
    return files;
  }

  public void setFiles(Map<String, InputStream> files) {
    this.files = files;
  }

  @Override
  public Source resolve(ResourceRequest request) throws XPathException {
    logger.debug("Resolving resource -> Relative URI: {}, Base URI: {}, URI: {}, Nature: {}", request.relativeUri, request.baseUri, request.uri, request.nature);
    if (request.relativeUri != null) {
      if (files.containsKey(request.relativeUri)) {
        logger.debug("Resource found in files map: {}", request.relativeUri);
        return new StreamSource(files.get(request.relativeUri));
      }
    }
    if(allowExternalResources()) {
      return next.resolve(request);
    }
    else{
      throw new XPathException("External resource access is not allowed: " + request.relativeUri);
    }
  }
}
