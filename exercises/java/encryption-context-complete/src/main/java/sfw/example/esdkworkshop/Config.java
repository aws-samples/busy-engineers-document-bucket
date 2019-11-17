// CHECKSTYLE:OFF MissingJavadocMethod
// TODO https://github.com/aws-samples/busy-engineers-document-bucket/issues/24

package sfw.example.esdkworkshop;

import com.moandjiezana.toml.Toml;
import java.io.File;

public class Config {
  private static final File DEFAULT_CONFIG = new File("../../config.toml");
  public static final ConfigContents contents =
      new Toml().read(DEFAULT_CONFIG).to(ConfigContents.class);

  private Config() { // Do not instantiate
  }

  // For automatic mpaping, these classes all have names dictated by the TOML file.
  // CHECKSTYLE:OFF MemberName
  // CHECKSTYLE:OFF ParameterName
  public static class ConfigContents {
    public final DocumentBucket document_bucket;

    ConfigContents(DocumentBucket document_bucket) {
      this.document_bucket = document_bucket;
    }
  }

  public static class DocumentBucket {
    public final DocumentTable document_table;

    DocumentBucket(DocumentTable document_table) {
      this.document_table = document_table;
    }
  }

  public static class DocumentTable {
    public final String name;

    public final String partition_key;

    public final String sort_key;

    DocumentTable(String name, String partition_key, String sort_key) {
      this.name = name;
      this.partition_key = partition_key;
      this.sort_key = sort_key;
    }
  }
  // CHECKSTYLE:ON MemberName
  // CHECKSTYLE:ON ParameterName
}
