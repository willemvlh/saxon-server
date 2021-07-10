package tv.mediagenix.transformer.app;

import java.io.IOException;

public class Utils {

    public static String getVersionNumber() {
        try {
            return new PropertiesReader().get("version");
        } catch (IOException e) {
            return "";
        }
    }


}
