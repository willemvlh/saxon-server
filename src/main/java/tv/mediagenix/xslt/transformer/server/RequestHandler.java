package tv.mediagenix.xslt.transformer.server;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;
import spark.Response;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonActor;
import tv.mediagenix.xslt.transformer.saxon.actors.SaxonActorBuilder;
import tv.mediagenix.xslt.transformer.saxon.core.SerializationProps;
import tv.mediagenix.xslt.transformer.saxon.core.TransformationException;
import tv.mediagenix.xslt.transformer.saxon.core.TypedInputStream;

public abstract class RequestHandler {
  protected Logger logger = LoggerFactory.getLogger(this.getClass());
  protected ServerOptions options = Server.getOptions();
  private Request request;
  private Response response;

  public Request getRequest() {
    return request;
  }

  private Collection<Part> getParts() throws IOException, ServletException {
    return this.request.raw().getParts();
  }

  protected abstract SaxonActorBuilder newBuilder();

  public RequestHandler(Request req, Response res) {
    this.request = req;
    this.response = res;
    setMultipartConfig();
    logParts();
  }

  private void setMultipartConfig() {
    request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/xxx/"));
  }

  private void logParts() {
    if (!logger.isDebugEnabled())
      return;
    try {
      this.getParts().forEach(part -> {
        logger.debug("Part: type={}, name={}, filename={}, size={}", part.getContentType(), part.getName(),
            part.getSubmittedFileName(), part.getSize());
        try {
          var buffer = getTypedInputStream(part).getInputStream().readAllBytes();
          logger.debug("Contents: {}", new String(buffer));
        } catch (IOException e) {
          logger.debug("Could not read part {}: {}", part.getName(), e.getMessage());
        }
      });
    } catch (Exception e) {
      logger.debug("Could not read parts: {}", e.getMessage());
    }
  }

  /* Return a Part's input, wrapping it in a GZIPInputStream if required */

  private TypedInputStream getTypedInputStream(Part part) {
    final String DEFAULT_MEDIA_TYPE = "application/xml";
    String contentEncoding = part.getHeader("Content-Encoding");
    String mediaType = DEFAULT_MEDIA_TYPE;
    if (part.getHeader("Content-Type") != null) {
      mediaType = extractMediaType(part.getHeader("Content-Type"));
    }
    InputStream stream;
    try {
      InputStream partStream = part.getInputStream();
      boolean isGzip = "gzip".equalsIgnoreCase(contentEncoding)
          || "application/gzip".equalsIgnoreCase(mediaType)
          || (part.getSubmittedFileName() != null && part.getSubmittedFileName().toLowerCase().endsWith(".gz"))
          || checkGzipMagicNumber(partStream);
      if (isGzip) {
        stream = new GZIPInputStream(part.getInputStream());
      } else {
        stream = part.getInputStream();
      }
    } catch (ZipException e) {
      logger.error("Could not unzip payload: {}", e.getMessage());
      throw new InvalidRequestException("Could not unzip payload: " + e.getMessage());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    if (mediaType == null || mediaType.isEmpty() || mediaType.equals("application/gzip")) {
      mediaType = DEFAULT_MEDIA_TYPE;
    }
    return new TypedInputStream(stream, mediaType);
  }

  private String extractMediaType(String contentType) {
    return contentType.split(";")[0].trim();
  }

  private boolean checkGzipMagicNumber(InputStream partStream) {
    var buffered = new BufferedInputStream(partStream);
    if (buffered.markSupported()) {
      try {
        buffered.mark(2);
        int magic = buffered.read() | (buffered.read() << 8);
        buffered.reset();
        return magic == GZIPInputStream.GZIP_MAGIC;
      } catch (IOException e) {
        logger.error("Could not read part stream to check for gzip magic number: {}", e.getMessage());
        return false;
      }
    } else {
      logger.warn("Part stream does not support mark/reset, cannot check for gzip magic number.");
      return false;
    }
  }

  public Void handle() throws InvalidRequestException, IOException, TransformationException {
    long startTime = System.currentTimeMillis();
    logParts();
    TypedInputStream input = getStream("xml").orElse(null);
    try {
      TypedInputStream stylesheet = getStream("xsl")
          .orElseThrow(() -> new InvalidRequestException("No XSL attachment found"));
      SaxonActor actor = this.newActor();
      ByteArrayOutputStream writeStream = new ByteArrayOutputStream();
      SerializationProps props = actor.act(input, stylesheet, writeStream);
      response.header("Content-Type", props.getContentType());
      var outputStream = response.raw().getOutputStream();
      writeStream.writeTo(outputStream);
      outputStream.close();
      return null;
    } finally {
      logger.info("Finished request {} in {} milliseconds", request.session().id(),
          System.currentTimeMillis() - startTime);
    }
  }

  private SaxonActor newActor() {
    try {
      return newBuilder()
          .setInsecure(options.isInsecure())
          .setBaseURI(options.getBaseURI())
          .setConfigurationFile(options.getConfigFile())
          .setTimeout(options.getTransformationTimeoutMs())
          .setParameters(getParameters("parameters"))
          .setSerializationProperties(getParameters("output"))
          .setFiles(getAdditionalFiles())
          .build();
    } catch (TransformationException e) {
      logger.error("Error during actor construction: ", e);
      throw new InvalidRequestException(e);
    }
  }

  private Map<String, InputStream> getAdditionalFiles() {
    try {
      return this.getParts().stream()
          .filter(part -> part.getSubmittedFileName() != null && !part.getSubmittedFileName().isEmpty())
          .collect(Collectors.toMap(part -> part.getSubmittedFileName(), part -> {
            return getTypedInputStream(part).getInputStream();
          }, (existing, incoming) -> {
            logger.warn("Duplicate file name found: {}. Using the first one.", existing);
            return existing;
          }));
    } catch (ServletException | IOException e) {
      throw new InvalidRequestException(e);
    }
  }

  private Map<String, String> getParameters(String key) {
    Optional<Part> part = getPart(key);
    return part.map(p -> {
      try {
        InputStream s = p.getInputStream();
        return new ParameterParser().parseStream(s, (int) p.getSize());
      } catch (IllegalArgumentException e) {
        logger.error("Could not parse parameters.", e);
        throw new InvalidRequestException(e.getMessage());
      } catch (IOException e) {
        logger.error("Could not read parameters due to IO error.", e);
        throw new UncheckedIOException(e);
      }
    }).orElseGet(() -> new HashMap<>());
  }

  private Optional<TypedInputStream> getStream(String key) {
    Optional<Part> part = getPart(key);
    return part.map(p -> getTypedInputStream(p));
  }

  private Optional<Part> getPart(String key) {
    try {
      return Optional.ofNullable(request.raw().getPart(key));
    } catch (ServletException e) {
      // request does not contain multipart data
      return Optional.empty();
    } catch (IOException e) {
      logger.error("Error retrieving part {}: {}", key, e.getMessage());
      throw new UncheckedIOException(e);
    }
  }

  public Response getResponse() {
    return response;
  }

}
