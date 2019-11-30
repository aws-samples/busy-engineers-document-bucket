package sfw.example.esdkworkshop;

// ADD-ESDK-COMPLETE: Add the ESDK Dependency
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * Entry point for writing logic to work with the Document Bucket, with a helper to obtain an API
 * instance to use.
 */
public class App {

  // The names of resources from the configuration file must exactly match those
  // keys for the automatic mapping.
  // CHECKSTYLE:OFF AbbreviationAsWordInName

  /**
   * Obtain a Document Bucket API initialized with the resources as configured by the bootstrap
   * configuration system.
   *
   * @return a new {@link Api} configured automatically by the bootstrapping system.
   */
  public static Api initializeDocumentBucket() {
    // Load the TOML State file with the information about launched CloudFormation resources
    State state = new State(Config.contents.base.state_file);

    // Configure DynamoDB client
    String tableName = state.contents.DocumentTable;
    AmazonDynamoDB ddbClient = AmazonDynamoDBClientBuilder.defaultClient();

    // Configure S3 client
    String bucketName = state.contents.DocumentBucket;
    AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

    // ADD-ESDK-COMPLETE: Configure the Faythe CMK in the Encryption SDK
    // Load configuration of KMS resources
    String faytheCMK = state.contents.FaytheCMK;

    // Set up the Master Key Provider to use KMS
    KmsMasterKeyProvider mkp =
        KmsMasterKeyProvider.builder().withKeysForEncryption(faytheCMK).build();

    return new Api(ddbClient, tableName, s3Client, bucketName, mkp);
  }
  // CHECKSTYLE:ON AbbreviationAsWordInName

  /**
   * Entry point for writing logic to interact with the Document Bucket system.
   *
   * @param args the command-line arguments to the Document Bucket.
   */
  public static void main(String[] args) {
    // Interact with the Document Bucket here or in jshell (mvn jshell:run)
  }
}
