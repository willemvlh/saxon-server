package io.github.willemvlh.transformer.app;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
class TransformerApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TransformerApplication.class);
        try {
            Logger logger = LoggerFactory.getLogger(TransformerApplication.class);
            logger.debug("Options: {}", String.join(", ", args));
            ServerOptions options = ServerOptions.fromArgs(args);
            Map<String, Object> optionsMap = new HashMap<>();
            optionsMap.put("server.port", options.getPort());
            optionsMap.put("logging.file.name", options.getLogFilePath());
            app.setDefaultProperties(optionsMap);
            app.setLogStartupInfo(false);
            app.setBannerMode(Banner.Mode.OFF);
            app.run(args);

        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }
}
