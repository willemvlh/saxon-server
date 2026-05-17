package tv.mediagenix.xslt.transformer.server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class ServerInfoTest{
  ServerInfo info = ServerInfo.getInstance().load();
  @Test   
  public void getSaxonServerVersion() {
    assertTrue(info.getSaxonServerVersion().matches("1\\.\\d+"));
  }
  @Test   
  public void getSaxonVersion(){
    assertTrue(info.getSaxonVersion().matches("HE [\\d\\.]+"));
  }

  @Test   
  public void getGitCommitHash(){
    System.out.println(info.getGitCommitHash());
    assertTrue(info.getGitCommitHash().matches("[0-9a-f]{7}"));
  }

  @Test   
  public void getBuildTimestamp(){
    var dt = ZonedDateTime.parse(info.getBuildTimestamp());
    assertTrue(dt.toEpochSecond() > LocalDateTime.of(2026,1,1,1,1).toEpochSecond(ZoneOffset.ofHours(0)));
  }

  @Test   
  public void getJDKVersion(){
    System.out.println(info.getJdkCompilerVersion());
    assertTrue(info.getJdkCompilerVersion().matches("[\\d\\.]+"));
  }
}
