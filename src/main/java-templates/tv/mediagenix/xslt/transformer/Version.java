package tv.mediagenix.xslt.transformer;

public class Version {
    public static final String VERSION          = "${project.version}";
    public static final String BUILD_TIMESTAMP  = "${build.timestamp}";
    public static final String JDK_VERSION      = "${java.version}";
    public static final String GIT_COMMIT       = "${git.commit.id.abbrev}";
}
