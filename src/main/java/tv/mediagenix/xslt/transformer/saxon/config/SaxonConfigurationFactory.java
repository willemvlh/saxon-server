package tv.mediagenix.xslt.transformer.saxon.config;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import net.sf.saxon.Configuration;

public abstract class SaxonConfigurationFactory {

  protected SaxonConfigurationFactory() {
  }

  private Map<String, InputStream> files = new HashMap<>();

  public Map<String, InputStream> getFiles() {
    return files;
  }

  public void setFiles(Map<String, InputStream> files) {
    this.files = files;
  }

  private URI baseURI = Paths.get("").toAbsolutePath().toUri();

  public URI getBaseURI() {
    return baseURI;
  }

  public void setBaseURI(URI baseUri) {
    this.baseURI = baseUri;
  }

  public abstract Configuration newConfiguration();
}
