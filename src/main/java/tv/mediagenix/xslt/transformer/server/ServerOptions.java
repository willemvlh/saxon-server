package tv.mediagenix.xslt.transformer.server;

import net.sf.saxon.s9api.Processor;
import org.apache.commons.cli.*;
import tv.mediagenix.xslt.transformer.server.ratelimiter.NoRateLimiter;
import tv.mediagenix.xslt.transformer.server.ratelimiter.RateLimiter;
import tv.mediagenix.xslt.transformer.server.ratelimiter.RateLimiterImpl;

import java.io.File;

public class ServerOptions {
    private Integer port = 5000;
    private File configFile;
    private boolean insecure = false;
    private long transformationTimeoutMs = 10000;
    private RateLimiter rateLimiter = new NoRateLimiter();
    private boolean debug = false;

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
        if (cmd.hasOption("rate-limit")) {
            serverOptions.setRateLimiter(createRateLimiter(cmd.getOptionValue("rate-limit")));
        }
        if (cmd.hasOption("debug")) {
            serverOptions.setDebug(true);
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
        options.addOption("r", "rate-limit", true, "Set up rate limiting (none | light | heavy)");
        options.addOption("d", "debug", false, "Enable debug logging statements");
        return options;
    }

    private static RateLimiter createRateLimiter(String option) {
        switch (option) {
            case "none":
                return new NoRateLimiter();
            case "light":
                return new RateLimiterImpl(60, 120);
            case "heavy":
                return new RateLimiterImpl(60, 60);
            default:
                throw new IllegalArgumentException("Unknown option: " + option);
        }
    }

    public void setRateLimiter(RateLimiter rl) {
        this.rateLimiter = rl;
    }

    private static void printInformation() {
        Processor p = new Processor(false);
        String version = p.getSaxonProductVersion();
        String edition = p.getSaxonEdition();
        System.out.printf("Saxon %s %s%n", edition, version);
    }

    public RateLimiter getRateLimiter() {
        return rateLimiter;
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
                String.format("Rate limiter: %s\n", rateLimiter.getClass().getSimpleName()) +
                String.format("Insecure: %s\n", insecure) +
                String.format("Config: %s \n", configFile == null ? "none" : configFile.getAbsolutePath());
    }
}
