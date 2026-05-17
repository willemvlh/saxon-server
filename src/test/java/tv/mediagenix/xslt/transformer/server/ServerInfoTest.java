package tv.mediagenix.xslt.transformer.server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

public class ServerInfoTest{
  @Test   
  public void getSaxonServerVersion() throws IOException{
    var info = ServerInfo.getInstance();
    System.out.println(info.getSaxonServerVersion());
    assertTrue(info.getSaxonServerVersion().matches("1\\.\\d+"));
  }
  @Test   
  public void getSaxonVersion(){
    var info = ServerInfo.getInstance();
    assertTrue(info.getSaxonVersion().matches("HE [\\d\\.]+"));
  }
}
