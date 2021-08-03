package io.github.willemvlh.morph.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {
    private final Properties properties;

    public PropertiesReader() throws IOException {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("properties-from-pom.properties")) {
            properties = new Properties();
            properties.load(is);
        }
    }

    public String get(String s) {
        return properties.getProperty(s);
    }
}
