// CHECKSTYLE:OFF MissingJavadocMethod
// TODO https://github.com/aws-samples/busy-engineers-document-bucket/issues/24

package sfw.example.esdkworkshop;

// ADD-ESDK-START: Add the ESDK Dependency
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

    // ADD-ESDK-START: Configure the Faythe CMK in the Encryption SDK
    // Construct the API
    return new Api(ddbClient, tableName, s3Client, bucketName);
  }
  // CHECKSTYLE:ON AbbreviationAsWordInName

  public static void main(String[] args) {
    // Interact with the Document Bucket here or in jshell (mvn jshell:run)
  }
}
