package tv.mediagenix.xslt.transformer.server;

import java.io.PrintStream;

import net.sf.saxon.s9api.Processor;
import tv.mediagenix.xslt.transformer.Version;

public class ServerInfo {
  private static final ServerInfo INSTANCE = new ServerInfo();

  public String getSaxonServerVersion() {
    return saxonServerVersion;
  }

  public String getGitCommitHash() {
    return gitCommitHash;
  }

  public String getJdkCompilerVersion() {
    return jdkCompilerVersion;
  }

  public String getBuildTimestamp() {
    return buildTimestamp;
  }

  public String getSaxonVersion() {
    return saxonVersion;
  }

  private String saxonServerVersion;
  private String gitCommitHash;
  private String jdkCompilerVersion;
  private String buildTimestamp;
  private String saxonVersion;

  public static ServerInfo getInstance() {
    return INSTANCE;
  }

  public ServerInfo load() {
    saxonVersion = computeSaxonVersion();
    saxonServerVersion = Version.VERSION;
    jdkCompilerVersion = Version.JDK_VERSION;
    gitCommitHash = Version.GIT_COMMIT;
    buildTimestamp = Version.BUILD_TIMESTAMP;
    return this;
  }

  private ServerInfo() {
  }

  public void print(PrintStream out){
    out.printf("Saxon Server version: %s%n", getSaxonServerVersion());
    out.printf("Saxon version:        %s%n", getSaxonVersion());
    out.printf("Git commit:           %s%n", getGitCommitHash());
    out.printf("JDK compiler version: %s%n", getJdkCompilerVersion());
    out.printf("Build timestamp:      %s%n", getBuildTimestamp());
  }

  private String computeSaxonVersion() {
    var proc = new Processor();
    var edition = proc.getSaxonEdition();
    var versionNumber = proc.getSaxonProductVersion();
    return edition + " " + versionNumber;
  }

}
