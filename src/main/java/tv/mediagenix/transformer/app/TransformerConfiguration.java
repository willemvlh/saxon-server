package tv.mediagenix.transformer.app;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.s9api.Processor;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import tv.mediagenix.transformer.saxon.TransformationException;
import tv.mediagenix.transformer.saxon.config.SaxonConfigurationFactory;
import tv.mediagenix.transformer.saxon.config.SaxonDefaultConfigurationFactory;
import tv.mediagenix.transformer.saxon.config.SaxonSecureConfigurationFactory;

import javax.xml.transform.stream.StreamSource;

@Component
public class TransformerConfiguration {
    private final ServerOptions options;

    @Autowired
    public TransformerConfiguration(ApplicationArguments args) throws ParseException {
        this.options = ServerOptions.fromArgs(args.getSourceArgs());
    }

    public TransformerConfiguration(String... args) throws ParseException {
        this.options = ServerOptions.fromArgs(args);
    }


    @Bean
    @Scope("singleton")
    public Processor getProcessor() throws TransformationException {
        try {
            Configuration config;
            SaxonConfigurationFactory factory = options.isInsecure()
                    ? new SaxonDefaultConfigurationFactory()
                    : new SaxonSecureConfigurationFactory();
            config = factory.newConfiguration();
            if (options.getLicenseFilepath() != null) {
                config.setConfigurationProperty(Feature.LICENSE_FILE_LOCATION, options.getLicenseFilepath());
            } else if (options.getConfigFile() != null) {
                config = Configuration.readConfiguration(new StreamSource(options.getConfigFile()));
            }
            return new Processor(config);
        } catch (Exception e) {
            throw new TransformationException(e.getMessage());
        }
    }

    @Bean
    public ServerOptions getServerOptions() {
        return options;
    }
}
