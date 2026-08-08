package tv.mediagenix.xslt.transformer.saxon.core;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ProtocolRestrictor;
import net.sf.saxon.lib.ResourceRequest;
import net.sf.saxon.lib.ResourceResolver;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.trans.XPathException;

/*
 * Allows accessing files sent as multipart form-data
 */
public class SaxonResourceResolver implements ResourceResolver, UnparsedTextURIResolver {

  private Map<String, InputStream> files = Map.of();
  private boolean allowExternalResources;
  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private ResourceResolver defaultResourceResolver;
  private UnparsedTextURIResolver defaultUnparsedTextURIResolver;
  private URI baseURI = null;
  private ProtocolRestrictor protocolRestrictor;

  public URI getBaseURI() {
    return baseURI;
  }

  public void setBaseURI(URI baseURI) {
    this.baseURI = baseURI;
  }

  public SaxonResourceResolver(boolean allowExternalResources, ResourceResolver defaultResourceResolver,
      UnparsedTextURIResolver unparsedTextURIResolver, ProtocolRestrictor protocolRestrictor) {
    this.allowExternalResources = allowExternalResources;
    this.defaultResourceResolver = defaultResourceResolver;
    this.defaultUnparsedTextURIResolver = unparsedTextURIResolver;
    this.protocolRestrictor = protocolRestrictor;
  }

  public Map<String, InputStream> getFiles() {
    return files;
  }

  public void setFiles(Map<String, InputStream> files) {
    this.files = files;
  }

  @Override
  public Source resolve(ResourceRequest request) throws XPathException {
    logger.debug("Resolving resource -> Relative URI: {}, Base URI: {}, URI: {}, Nature: {}", request.relativeUri,
        request.baseUri, request.uri, request.nature);
    if (request.relativeUri != null && files.containsKey(request.relativeUri)) {
      logger.debug("Resource found in files map: {}", request.relativeUri);
      return new StreamSource(files.get(request.relativeUri));
    }
    if (allowExternalResources && protocolRestrictor.test(URI.create(request.uri))) {
      logger.debug("Resource not found in files map, delegating to default resource resolver");
      return defaultResourceResolver.resolve(request);
    }
    throw new XPathException("External resource access is not allowed: " + request.relativeUri);
  }

  @Override
  public Reader resolve(URI absoluteURI, String encoding, Configuration config) throws XPathException {
    logger.debug("Resolving unparsed text URI -> Base URI: {}, Absolute URI: {}, Encoding: {}", baseURI, absoluteURI,
        encoding);
    /*
     * Unfortunately, we only get an absolute URI, which makes it hard to tell how
     * to resolve the request against any files supplied in the HTTP request.
     * To work around this, we subtract the base URI set by the application from the
     * absoluteURI parameter.
     */

    var relativeURI = baseURI.relativize(absoluteURI);
    if (files.containsKey(relativeURI.toString())) {
      logger.debug("Resource found in files map: {}", relativeURI);
      try {
        return new InputStreamReader(files.get(relativeURI.toString()), encoding != null ? encoding : "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new XPathException("Unsupported encoding: " + encoding, e);
      }
    }
    if (allowExternalResources && protocolRestrictor.test(absoluteURI)) {
      logger.debug("Resource not found in files map, delegating to default resource resolver");
      return defaultUnparsedTextURIResolver.resolve(absoluteURI, encoding, config);
    } else {
      throw new XPathException("External resource access is not allowed: " + absoluteURI);
    }
  }
}
