package tv.mediagenix.transformer;

import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestOptions {

    private ApplicationArguments applicationArguments;

    TestOptions(ApplicationArguments applicationArguments) {
        this.applicationArguments = applicationArguments;
    }

    public void printArguments() {
        for (String arg : applicationArguments.getSourceArgs()) {
            System.out.println(arg);
        }
    }
}
