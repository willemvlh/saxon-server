package io.github.willemvlh.transformer.app;

import net.sf.saxon.s9api.Processor;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

public class ServerOptions {
    private final int DEFAULT_PORT = 5000;
    private final int DEFAULT_TIMEOUT_MS = 10 * 60 * 1000; // 10 minutes

    private int port = DEFAULT_PORT;
    private File configFile;
    private boolean insecure = false;
    private long transformationTimeoutMs = DEFAULT_TIMEOUT_MS;
    private String logFilePath;

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

    public boolean isInsecure() {
        return insecure;
    }

    private void setInsecure(boolean insecure) {
        this.insecure = insecure;
    }

    public static ServerOptions fromArgs(String... args) throws ParseException {
        return fromArgs(System.out, true, args);
    }

    public static ServerOptions fromArgs(OutputStream out, boolean quitAfterMessage, String... args) throws ParseException {
        ServerOptions serverOptions = new ServerOptions();
        Options options = new Options();
        options.addOption("p", "port", true, "Port on which the server runs");
        options.addOption("c", "config", true, "Location to Saxon configuration XML file");
        options.addOption("v", "version", false, "Display Saxon version info");
        options.addOption("h", "help", false, "Display help");
        options.addOption("i", "insecure", false, "Run with Saxon's default (insecure) configuration");
        options.addOption("t", "timeout", true, "The maximum time a transformation is allowed to run in milliseconds.");
        options.addOption("o", "output", true, "Write console output to the specified file");
        CommandLineParser p = new DefaultParser();
        CommandLine cmd = p.parse(options, args);
        if (cmd.hasOption("help")) {
            printHelp(out, options);
            if (quitAfterMessage) System.exit(0);
        }
        if (cmd.hasOption("version")) {
            printInformation(out);
            if (quitAfterMessage) System.exit(0);
        }
        if (cmd.hasOption("port")) {
            try {
                int portAsInt = Integer.parseInt(cmd.getOptionValue("port"));
                if (portAsInt < 0 || portAsInt > 65535) {
                    throw new NumberFormatException();
                }
                serverOptions.setPort(portAsInt);
            } catch (NumberFormatException e) {
                throw new ParseException("Illegal value for port. Must be a positive integer between 0 and 65535");
            }
        }
        if (cmd.hasOption("config")) {
            if (cmd.hasOption("insecure")) {
                throw new RuntimeException("Options 'config' and 'insecure' are mutually exclusive.");
            }
            File config = new File(cmd.getOptionValue("config"));
            if (!config.exists()) {
                throw new RuntimeException("Configuration file could not be found.");
            }
            serverOptions.setConfigFile(config);
        }

        if (cmd.hasOption("insecure")) {
            serverOptions.setInsecure(true);
        }

        if (cmd.hasOption("timeout")) {
            try {
                long timeout = Long.parseLong(cmd.getOptionValue("timeout"));
                if (timeout < -1) {
                    throw new NumberFormatException();
                }
                serverOptions.setTransformationTimeoutMs(timeout);
            } catch (NumberFormatException e) {
                throw new ParseException(String.format("Illegal value for timeout parameter. Must be an integer between -1 and %d", Long.MAX_VALUE));
            }
        }

        if (cmd.hasOption("output")) {
            serverOptions.setLogFilePath(cmd.getOptionValue("output"));
        }

        return serverOptions;
    }

    private static void printHelp(OutputStream out, Options options) {
        PrintWriter writer = new PrintWriter(out);
        HelpFormatter formatter = new HelpFormatter();
        String cmdLine = String.format("java -jar saxon-server-%s.jar", Utils.getVersionNumber());
        formatter.printHelp(writer, formatter.getWidth(), cmdLine, null, options, formatter.getLeftPadding(), formatter.getDescPadding(), null);
        writer.flush();
    }

    private static void printInformation(OutputStream out) {
        Processor p = new Processor(false);
        String version = p.getSaxonProductVersion();
        String edition = p.getSaxonEdition();
        PrintWriter writer = new PrintWriter(out);
        writer.printf("Saxon %s %s%n", edition, version);
        writer.flush();
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }
}