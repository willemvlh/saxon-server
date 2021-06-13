package tv.mediagenix.transformer.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TransformerApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TransformerApplication.class);
        app.setLogStartupInfo(false);
        app.run(args);
    }
}
