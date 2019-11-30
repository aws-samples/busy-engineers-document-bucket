package sfw.example.esdkworkshop;

import com.moandjiezana.toml.Toml;
import java.io.File;

/** Helper to pull required Document Bucket configuration keys out of the configuration system. */
public class Config {
  private static final File DEFAULT_CONFIG = new File("../../config.toml");
  public static final ConfigContents contents =
      new Toml().read(DEFAULT_CONFIG).to(ConfigContents.class);

  private Config() { // Do not instantiate
  }

  // For automatic mpaping, these classes all have names dictated by the TOML file.
  // CHECKSTYLE:OFF MemberName
  // CHECKSTYLE:OFF ParameterName

  /** The top-level contents of the configuration file. */
  public static class ConfigContents {
    /** The [base] section of the configuration file. */
    public final Base base;
    /** The [document_bucket] section of the configuration file. */
    public final DocumentBucket document_bucket;

    ConfigContents(Base base, DocumentBucket document_bucket) {
      this.base = base;
      this.document_bucket = document_bucket;
    }
  }

  /** The [base] section of the configuration file. */
  public static class Base {
    /** The location of the state file for CloudFormation-managed AWS resource identifiers. */
    public final String state_file;

    Base(String state_file) {
      this.state_file = state_file;
    }
  }

  /** The [document_bucket] section of the configuration file. */
  public static class DocumentBucket {
    /** The [document_bucket.document_table] section of the configuration file. */
    public final DocumentTable document_table;
    /** The [document_bucket.bucket] section of the configuration file. */
    public final Bucket bucket;

    DocumentBucket(DocumentTable document_table, Bucket bucket) {
      this.document_table = document_table;
      this.bucket = bucket;
    }
  }

  /** The [document_bucket.document_table] section of the configuration file. */
  public static class DocumentTable {
    /** Table name. */
    public final String name;

    /** Table partition key name on items. */
    public final String partition_key;

    /** Item sort key name on items. */
    public final String sort_key;

    /** The identifier for pointer records indicating that this record is for an S3 object. */
    public final String object_target;

    /**
     * The prefix for context records to indicate that this record is for a list of documents
     * matching a context key.
     */
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

  /** The [document_bucket.bucket] section of the configuration file. */
  public static class Bucket {
    /** Bucket name. */
    public final String name;
    /** The identifier of the CloudFormation output. */
    public final String output;
    /** The identifier of the CloudFormation export. */
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
