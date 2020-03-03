package XsltTransformer;

import org.apache.commons.cli.*;

import java.io.File;

public class ServerOptions {
    private Integer Port;
    private File ConfigFile;

    public Integer getPort() {
        return Port;
    }

    public void setPort(int port) {
        Port = port;
    }

    public File getConfigFile() {
        return ConfigFile;
    }

    public void setConfigFile(File configFile) {
        ConfigFile = configFile;
    }

    public static ServerOptions fromArgs(String[] args) throws ParseException {
        ServerOptions serverOptions = new ServerOptions();
        Options options = new Options();
        options.addOption("port", "Port on which the server runs");
        options.addOption("config", "Location to Saxon configuration XML");
        CommandLineParser p = new DefaultParser();
        CommandLine cmd = p.parse(options, args);
        if(cmd.hasOption("port")){
            try{
                int portAsInt = Integer.valueOf(cmd.getOptionValue("port"));
                serverOptions.setPort(portAsInt);
            }
            catch(NumberFormatException e){
             throw new ParseException("Could not parse port (must be integer)");
            }
        }
        if(cmd.hasOption("config")){
            serverOptions.setConfigFile(new File(cmd.getOptionValue("config")));
        }
        return serverOptions;
    }
}
