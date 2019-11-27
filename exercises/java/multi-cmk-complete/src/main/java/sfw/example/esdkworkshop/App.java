// CHECKSTYLE:OFF MissingJavadocMethod
// TODO https://github.com/aws-samples/busy-engineers-document-bucket/issues/24

package sfw.example.esdkworkshop;

import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class App {

  // The names of resources from the configuration file must exactly match those
  // keys for the automatic mapping.
  // CHECKSTYLE:OFF AbbreviationAsWordInName
  public static Api initializeDocumentBucket() {
    // Load the TOML State file with the information about launched CloudFormation resources
    State state = new State(Config.contents.base.state_file);

    // Configure DynamoDB client
    String tableName = state.contents.DocumentTable;
    AmazonDynamoDB ddbClient = AmazonDynamoDBClientBuilder.defaultClient();

    // Configure S3 client
    String bucketName = state.contents.DocumentBucket;
    AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

    // Load configuration of KMS resources
    String faytheCMK = state.contents.FaytheCMK;
    // MULTI-CMK-COMPLETE: Configure Walter
    String walterCMK = state.contents.WalterCMK;

    // Set up the Master Key Provider to use KMS
    // MULTI-CMK-COMPLETE: Add Walter to the CMKs to Use
    KmsMasterKeyProvider mkp =
        KmsMasterKeyProvider.builder().withKeysForEncryption(faytheCMK, walterCMK).build();

    return new Api(ddbClient, tableName, s3Client, bucketName, mkp);
  }
  // CHECKSTYLE:ON AbbreviationAsWordInName

  public static void main(String[] args) {
    // Interact with the Document Bucket here or in jshell (mvn jshell:run)
  }
}
