package sfw.example.esdkworkshop;

import com.moandjiezana.toml.Toml;
import java.io.File;

// For automatic mpaping, these classes all have names dictated by the TOML file.
// CHECKSTYLE:OFF MemberName
// CHECKSTYLE:OFF ParameterName
public class State {
  public final Contents contents;

  public State(String path) {
    contents = new Toml().read(new File(path)).to(Contents.class);
  }

  public static class Contents {
    public final String DocumentBucket;
    public final String DocumentTable;
    public final String FaytheCMK;
    public final String WalterCMK;

    Contents(String DocumentBucket, String DocumentTable, String FaytheCMK, String WalterCMK) {
      this.DocumentBucket = DocumentBucket;
      this.DocumentTable = DocumentTable;
      this.FaytheCMK = FaytheCMK;
      this.WalterCMK = WalterCMK;
    }
  }
}
