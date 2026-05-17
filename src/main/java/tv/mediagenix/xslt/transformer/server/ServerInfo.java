package tv.mediagenix.xslt.transformer.server;

import net.sf.saxon.s9api.Processor;
import tv.mediagenix.xslt.transformer.Version;

public class ServerInfo {
  private static final ServerInfo INSTANCE = new ServerInfo();

  private String saxonVersion;
  private String saxonServerVersion;

  public static ServerInfo getInstance() {
    return INSTANCE;
  }

  public ServerInfo load(){
    this.getSaxonVersion();
    this.getSaxonServerVersion();
    return this;
  }

  private ServerInfo() {
  }

  public String getSaxonVersion() {
    if (saxonVersion == null) {
      var proc = new Processor();
      var edition = proc.getSaxonEdition();
      var versionNumber = proc.getSaxonProductVersion();
      saxonVersion = edition + " " + versionNumber;
    }
    return saxonVersion;
  }

  public String getSaxonServerVersion() {
    if (saxonServerVersion == null) {
      saxonServerVersion = Version.VERSION;
    }
    return saxonServerVersion;
  }
}
