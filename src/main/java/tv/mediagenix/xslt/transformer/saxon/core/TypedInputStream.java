package tv.mediagenix.xslt.transformer.saxon.core;

import java.io.InputStream;

public class TypedInputStream {
  private String mediaType;

  private InputStream inputStream;

  public String getMediaType() {
    return mediaType;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public TypedInputStream(InputStream stream, String mediaType) {
    this.mediaType = mediaType;
    this.inputStream = stream;
  }

  public TypedInputStream(InputStream stream) {
    this(stream, "application/xml");
  }

  public boolean isApplicationJson(){
    return this.mediaType.equals("application/json");
  }
}
