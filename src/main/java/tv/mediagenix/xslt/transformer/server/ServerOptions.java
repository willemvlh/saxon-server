package tv.mediagenix.xslt.transformer.server;

import tv.mediagenix.xslt.transformer.Version;

import org.apache.commons.cli.*;
import java.io.File;
import java.net.URI;
import java.nio.file.Paths;

public class ServerOptions {
  private Integer port = 5000;
  private File configFile;
  private boolean insecure = false;
  private long transformationTimeoutMs = 2 * 60 * 1000;
  private boolean debug = false;
  private boolean disableFrontend = false;
  private URI baseURI = Paths.get("").toAbsolutePath().toUri();

  public URI getBaseURI() {
    return baseURI;
  }

  public void setBaseURI(URI baseURI) {
    this.baseURI = baseURI;
  }

  public boolean isDisableFrontend() {
    return disableFrontend;
  }

  public void setDisableFrontend(boolean disableFrontend) {
    this.disableFrontend = disableFrontend;
  }

  public Integer getPort() {
    return port;
  }

  private void setPort(int port) {
    this.port = port;
  }

  public long getTransformationTimeoutMs() {
    return transformationTimeoutMs;
  }

  private void setTransformationTimeoutMs(long milliseconds) {
    this.transformationTimeoutMs = milliseconds;
  }

  public File getConfigFile() {
    return configFile;
  }

  private void setConfigFile(File configFile) {
    this.configFile = configFile;
  }

  public boolean isInsecure() {
    return insecure;
  }

  private void setInsecure(boolean insecure) {
    this.insecure = insecure;
  }

  public static ServerOptions fromArgs(String[] args) throws ParseException, InvalidOptionException {
    ServerOptions serverOptions = new ServerOptions();
    Options options = getOptions();
    CommandLineParser p = new DefaultParser();
    CommandLine cmd = p.parse(options, args);
    if (cmd.hasOption("help")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("java -jar saxon-server-" + Version.VERSION + ".jar <options>", options);
      System.exit(0);
    }
    if (cmd.hasOption("version")) {
      printInformation();
      System.exit(0);
    }
    if (cmd.hasOption("port")) {
      try {
        int portAsInt = Integer.parseInt(cmd.getOptionValue("port"));
        serverOptions.setPort(portAsInt);
      } catch (NumberFormatException e) {
        throw new InvalidOptionException("Port must be a number", e);
      }
    }
    if (cmd.hasOption("config")) {
      if (cmd.hasOption("insecure")) {
        throw new InvalidOptionException("Options 'config' and 'insecure' are mutually exclusive.");
      }
      serverOptions.setConfigFile(new File(cmd.getOptionValue("config")));
    }

    if (cmd.hasOption("insecure")) {
      serverOptions.setInsecure(true);
    }
    if (cmd.hasOption("timeout")) {
      try {
        Long timeoutValue = Long.parseLong(cmd.getOptionValue("timeout"));
        serverOptions.setTransformationTimeoutMs(timeoutValue);
      } catch (NumberFormatException e) {
        throw new InvalidOptionException("Timeout must be a number", e);
      }
    }
    if (cmd.hasOption("debug")) {
      serverOptions.setDebug(true);
    }
    if (cmd.hasOption("disable-frontend")) {
      serverOptions.setDisableFrontend(true);
    }
    if (cmd.hasOption("base-uri")) {
      try {
        URI baseUri = URI.create(cmd.getOptionValue("base-uri"));
        serverOptions.setBaseURI(baseUri);
      } catch (IllegalArgumentException e) {
        throw new InvalidOptionException("Base URI must be a valid URI", e);
      }
    }

    return serverOptions;
  }

  private static Options getOptions() {
    Options options = new Options();
    options.addOption("p", "port", true, "Port on which the server runs");
    options.addOption("c", "config", true, "Location to Saxon configuration XML file");
    options.addOption("v", "version", false, "Display Saxon version info");
    options.addOption("h", "help", false, "Display help");
    options.addOption("i", "insecure", false, "Run with default (insecure) configuration");
    options.addOption("t", "timeout", true, "The maximum time a transformation is allowed to run in milliseconds.");
    options.addOption("d", "debug", false, "Enable debug logging statements");
    options.addOption("b", "base-uri", true, "Base URI for resolving relative URIs");
    options.addOption("df", "disable-frontend", false, "Disable the frontend (HTML) interface");
    return options;
  }

  private static void printInformation() {
    ServerInfo info = ServerInfo.getInstance().load();
    System.out.printf("Saxon Server version: %s%n", info.getSaxonServerVersion());
    System.out.printf("Saxon version:        %s%n", info.getSaxonVersion());
    System.out.printf("Git commit:           %s%n", info.getGitCommitHash());
    System.out.printf("JDK compiler version: %s%n", info.getJdkCompilerVersion());
    System.out.printf("Build timestamp:      %s%n", info.getBuildTimestamp());
  }

  public boolean isDebuggingEnabled() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public String toString() {
    return String.format("Debug: %s\n", isDebuggingEnabled()) +
        String.format("Port: %s\n", port) +
        String.format("Insecure: %s\n", insecure) +
        String.format("Config: %s \n", configFile == null ? "none" : configFile.getAbsolutePath());
  }
}
