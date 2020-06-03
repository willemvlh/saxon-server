package tv.mediagenix.xslt.transformer;

import net.sf.saxon.s9api.Processor;
import org.apache.commons.cli.*;

import java.io.File;

public class ServerOptions {
    private Integer port;
    private File configFile;

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

    public static ServerOptions fromArgs(String[] args) throws ParseException {
        ServerOptions serverOptions = new ServerOptions();
        Options options = new Options();
        options.addOption("p", "port", true, "Port on which the server runs");
        options.addOption("c", "config", true, "Location to Saxon configuration XML file");
        options.addOption("v", "version", false, "Display Saxon version info");
        options.addOption("h", "help", false, "Display help");
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
            serverOptions.setConfigFile(new File(cmd.getOptionValue("config")));
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
