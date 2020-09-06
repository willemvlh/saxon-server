package tv.mediagenix.xslt.transformer.saxon;

import java.io.File;

public class SaxonOptions {
    private boolean isInsecure;
    private File configFile;

    public boolean isInsecure() {
        return isInsecure;
    }

    public void setInsecure(boolean insecure) {
        isInsecure = insecure;
    }

    public File getConfigFile() {
        return configFile;
    }

    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }
}
