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
    public final Base base;
    public final DocumentBucket document_bucket;

    ConfigContents(Base base, DocumentBucket document_bucket) {
      this.base = base;
      this.document_bucket = document_bucket;
    }
  }

  public static class Base {
    public final String state_file;

    Base(String state_file) {
      this.state_file = state_file;
    }
  }

  public static class DocumentBucket {
    public final DocumentTable document_table;
    public final Bucket bucket;

    DocumentBucket(DocumentTable document_table, Bucket bucket) {
      this.document_table = document_table;
      this.bucket = bucket;
    }
  }

  public static class DocumentTable {
    public final String name;

    public final String partition_key;

    public final String sort_key;

    public final String object_target;

    public final String ctx_prefix;

    DocumentTable(
        String name,
        String partition_key,
        String sort_key,
        String object_target,
        String ctx_prefix) {
      this.name = name;
      this.partition_key = partition_key;
      this.sort_key = sort_key;
      this.object_target = object_target;
      this.ctx_prefix = ctx_prefix;
    }
  }

  public static class Bucket {
    public final String name;
    public final String output;
    public final String export;

    Bucket(String name, String output, String export) {
      this.name = name;
      this.output = output;
      this.export = export;
    }
  }
  // CHECKSTYLE:ON MemberName
  // CHECKSTYLE:ON ParameterName
}
