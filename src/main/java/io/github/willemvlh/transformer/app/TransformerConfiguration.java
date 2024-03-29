package io.github.willemvlh.transformer.app;

import io.github.willemvlh.transformer.saxon.TransformationException;
import io.github.willemvlh.transformer.saxon.config.SaxonConfigurationFactory;
import io.github.willemvlh.transformer.saxon.config.SaxonDefaultConfigurationFactory;
import io.github.willemvlh.transformer.saxon.config.SaxonSecureConfigurationFactory;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.xml.transform.stream.StreamSource;

@Component
public class TransformerConfiguration {
    private final ServerOptions options;

    @Autowired
    public TransformerConfiguration(ApplicationArguments args) throws ParseException {
        this.options = ServerOptions.fromArgs(args.getSourceArgs());
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
           if (options.getConfigFile() != null) {
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
