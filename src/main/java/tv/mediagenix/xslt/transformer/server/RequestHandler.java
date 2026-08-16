package tv.mediagenix.xslt.transformer.server;

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

public abstract class RequestHandler {
  protected Logger logger = LoggerFactory.getLogger(this.getClass());
  private Request request;
  private Response response;
  private ServerOptions options;

  public ServerOptions getOptions() {
    return options;
  }

  public void setOptions(ServerOptions options) {
    this.options = options;
  }

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
          var buffer = getStreamFromPart(part).readAllBytes();
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

  private InputStream getStreamFromPart(Part part) {
    String contentType = part.getContentType();
    try {
      if ("application/gzip".equalsIgnoreCase(contentType)) {
        logger.debug("Payload is zipped");
        return new GZIPInputStream(part.getInputStream());
      }
      return part.getInputStream();
    } catch (ZipException e) {
      logger.error("Could not unzip payload: {}", e.getMessage());
      throw new InvalidRequestException("Could not unzip payload: " + e.getMessage());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public void handle() throws InvalidRequestException, IOException, TransformationException {
    long startTime = System.currentTimeMillis();
    logParts();
    Optional<InputStream> input = getStream("xml");
    try (InputStream stylesheet = getStream("xsl")
        .orElseThrow(() -> new InvalidRequestException("No XSL attachment found"))) {
      SaxonActor actor = this.newActor();
      ByteArrayOutputStream writeStream = new ByteArrayOutputStream();
      SerializationProps props = input.isPresent()
          ? actor.act(input.get(), stylesheet, writeStream)
          : actor.act(stylesheet, writeStream);
      response.header("Content-Type", props.getContentType());
      var outputStream = response.raw().getOutputStream();
      writeStream.writeTo(outputStream);
      outputStream.close();
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
            return getStreamFromPart(part);
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
      } catch (IOException | IllegalArgumentException e) {
        logger.error("Could not read parameters.", e);
        throw new InvalidRequestException(e.getMessage());
      }
    }).orElseGet(() -> new HashMap<>());
  }

  private Optional<InputStream> getStream(String key) {
    try {
      Optional<Part> part = getPart(key);
      return part.map(p -> getStreamFromPart(p));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private Optional<Part> getPart(String key) throws IOException {
    try {
      return Optional.of(request.raw().getPart(key));
    } catch (ServletException e) {
      // request does not contain multipart data
      return Optional.empty();
    }
  }

  public Response getResponse() {
    return response;
  }

}
