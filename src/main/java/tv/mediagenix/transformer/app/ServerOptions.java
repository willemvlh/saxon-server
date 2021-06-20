package tv.mediagenix.transformer.app;

import net.sf.saxon.s9api.Processor;
import org.apache.commons.cli.*;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ServerOptions {
    private int port = 5000;
    private File configFile;
    private boolean insecure = false;
    private long transformationTimeoutMs = 10000;
    private String logFilePath;
    private String licenseFilepath;

    public int getPort() {
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

    public String getLicenseFilepath() {
        return licenseFilepath;
    }

    private void setLicenseFilepath(String filepath) {
        this.licenseFilepath = filepath;
    }

    public boolean isInsecure() {
        return insecure;
    }

    private void setInsecure(boolean insecure) {
        this.insecure = insecure;
    }

    public static ServerOptions fromArgs(String[] args) throws ParseException {
        ServerOptions serverOptions = new ServerOptions();
        Options options = new Options();
        options.addOption("p", "port", true, "Port on which the server runs");
        options.addOption("c", "config", true, "Location to Saxon configuration XML file");
        options.addOption("v", "version", false, "Display Saxon version info");
        options.addOption("h", "help", false, "Display help");
        options.addOption("i", "insecure", false, "Run with Saxon's default (insecure) configuration");
        options.addOption("t", "timeout", true, "The maximum time a transformation is allowed to run in milliseconds.");
        options.addOption("o", "output", true, "Write console output to the specified file");
        options.addOption("l", "license", true, "Path to license file");
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

        if (cmd.hasOption("output")) {
            serverOptions.setLogFilePath(cmd.getOptionValue("output"));
        }

        if (cmd.hasOption("license")) {
            serverOptions.setLicenseFilepath(cmd.getOptionValue("license"));
        }

        return serverOptions;
    }

    private static void printInformation() {
        Processor p = new Processor(false);
        String version = p.getSaxonProductVersion();
        String edition = p.getSaxonEdition();
        System.out.printf("Saxon %s %s%n", edition, version);

    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }
}
