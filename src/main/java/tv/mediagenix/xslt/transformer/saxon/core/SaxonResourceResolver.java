package tv.mediagenix.xslt.transformer.saxon.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.lib.ProtocolRestrictor;
import net.sf.saxon.lib.ResourceRequest;
import net.sf.saxon.lib.ResourceResolver;
import net.sf.saxon.trans.XPathException;

/*
 * Allows accessing files sent as multipart form-data
 */
public class SaxonResourceResolver implements ResourceResolver {

  private Map<String, InputStream> files = Map.of();
  private boolean allowExternalResources;
  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private ResourceResolver defaultResourceResolver;
  private URI baseURI = null;
  private ProtocolRestrictor protocolRestrictor;

  public URI getBaseURI() {
    return baseURI;
  }

  public void setBaseURI(URI baseURI) {
    this.baseURI = baseURI;
  }

  public SaxonResourceResolver(boolean allowExternalResources, ResourceResolver defaultResourceResolver,
      ProtocolRestrictor protocolRestrictor) {
    this.allowExternalResources = allowExternalResources;
    this.defaultResourceResolver = defaultResourceResolver;
    this.protocolRestrictor = protocolRestrictor;
  }

  public Map<String, InputStream> getFiles() {
    return files;
  }

  public void setFiles(Map<String, InputStream> files) {
    for (Map.Entry<String, InputStream> entry : files.entrySet()) {
      var stream = entry.getValue();
      if (!stream.markSupported()) {
        logger.warn(
            "Input stream for file: {} does not support mark/reset!",
            entry.getKey());
      } else {
        stream.mark(Integer.MAX_VALUE);
      }
    }
    this.files = files;
  }

  /*
   * Handles both XML resources (doc(), xsl:include, ...) and non-XML resources
   * (unparsed-text(), json-doc(), ...). For the latter, Saxon's default
   * StandardUnparsedTextResolver delegates to the configuration's ResourceResolver,
   * but only supplies the absolute URI. In that case we derive the relative URI from
   * the base URI set by the application.
   */
  @Override
  public Source resolve(ResourceRequest request) throws XPathException {
    logger.debug("Resolving resource -> Relative URI: {}, Base URI: {}, URI: {}, Nature: {}", request.relativeUri,
        request.baseUri, request.uri, request.nature);
    var key = findFileKey(request);
    if (key != null) {
      logger.debug("Resource found in files map: {}", key);
      var stream = files.get(key);
      try {
        stream.reset();
      } catch (IOException e) {
        logger.warn("Could not reset stream for resource: {}. Multiple reads may deliver inconsistent results",
            key, e);
      }
      return new StreamSource(stream, request.uri);
    }
    if (allowExternalResources && request.uri != null && protocolRestrictor.test(URI.create(request.uri))) {
      logger.debug("Resource not found in files map, delegating to default resource resolver");
      return defaultResourceResolver == null ? null : defaultResourceResolver.resolve(request);
    }
    throw new XPathException("External resource access is not allowed: " + relativeUri(request));
  }

  private String findFileKey(ResourceRequest request) {
    if (request.relativeUri != null && files.containsKey(request.relativeUri)) {
      return request.relativeUri;
    }
    var relative = relativeUri(request);
    return relative != null && files.containsKey(relative) ? relative : null;
  }

  private String relativeUri(ResourceRequest request) {
    if (request.relativeUri != null) {
      return request.relativeUri;
    }
    if (request.uri != null && baseURI != null) {
      return baseURI.relativize(URI.create(request.uri)).toString();
    }
    return request.uri;
  }
}
