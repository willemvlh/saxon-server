package tv.mediagenix.xslt.transformer.server;

import net.sf.saxon.s9api.Processor;
import org.apache.commons.cli.*;

import java.io.File;

public class ServerOptions {
    private Integer port = 5000;
    private File configFile;
    private boolean insecure = false;

    public Integer getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public File getConfigFile() {
        return configFile;
    }

    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }

    public boolean isInsecure() {
        return insecure;
    }

    public void setInsecure(boolean insecure) {
        this.insecure = insecure;
    }


    public static ServerOptions fromArgs(String[] args) throws ParseException {
        ServerOptions serverOptions = new ServerOptions();
        Options options = new Options();
        options.addOption("p", "port", true, "Port on which the server runs");
        options.addOption("c", "config", true, "Location to Saxon configuration XML file");
        options.addOption("v", "version", false, "Display Saxon version info");
        options.addOption("h", "help", false, "Display help");
        options.addOption("i", "insecure", false, "Run with default (insecure) configuration");
        CommandLineParser p = new DefaultParser();
        CommandLine cmd = p.parse(options, args);
        if (cmd.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar saxon-servlet-XX.jar", options);
            System.exit(0);
        }
        if (cmd.hasOption("version")) {
            printInformation();
            System.exit(0);
        }
        if (cmd.hasOption("port")) {
            try {
                int portAsInt = Integer.valueOf(cmd.getOptionValue("port"));
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
        return serverOptions;
    }

    private static void printInformation() {
        Processor p = new Processor(false);
        String version = p.getSaxonProductVersion();
        String edition = p.getSaxonEdition();
        System.out.println(String.format("Saxon %s %s", edition, version));

    }

}
