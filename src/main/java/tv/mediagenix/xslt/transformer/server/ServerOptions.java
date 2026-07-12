package tv.mediagenix.xslt.transformer.server;

import net.sf.saxon.s9api.Processor;
import org.apache.commons.cli.*;
import java.io.File;

public class ServerOptions {
    private Integer port = 5000;
    private File configFile;
    private boolean insecure = false;
    private long transformationTimeoutMs = 2 * 60 * 1000;
    private boolean debug = false;
    private boolean disableFrontend = false;

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

    public static ServerOptions fromArgs(String[] args) throws ParseException {
        ServerOptions serverOptions = new ServerOptions();
        Options options = getOptions();
        CommandLineParser p = new DefaultParser();
        CommandLine cmd = p.parse(options, args);
        if (cmd.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar saxon-server-XX.jar", options);
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
                throw new ParseException(e.getMessage());
            }
        }
        if (cmd.hasOption("config")) {
            if (cmd.hasOption("insecure")) {
                throw new RuntimeException("Options 'config' and 'insecure' are mutually exclusive.");
            }
            serverOptions.setConfigFile(new File(cmd.getOptionValue("config")));
        }

        if (cmd.hasOption("insecure")) {
            serverOptions.setInsecure(true);
        }
        if (cmd.hasOption("timeout")) {
            serverOptions.setTransformationTimeoutMs(Long.parseLong(cmd.getOptionValue("timeout")));
        }
        if (cmd.hasOption("debug")) {
            serverOptions.setDebug(true);
        }
        if (cmd.hasOption("disable-frontend")) {
            serverOptions.setDisableFrontend(true);
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
        options.addOption("df", "disable-frontend", false, "Disable the frontend (HTML) interface");
        return options;
    }

    private static void printInformation() {
        Processor p = new Processor(false);
        String version = p.getSaxonProductVersion();
        String edition = p.getSaxonEdition();
        System.out.printf("Saxon %s %s%n", edition, version);
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
