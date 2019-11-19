// CHECKSTYLE:OFF MissingJavadocMethod
// TODO https://github.com/aws-samples/busy-engineers-document-bucket/issues/24

package sfw.example.esdkworkshop;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.MasterKeyProvider;
import com.amazonaws.encryptionsdk.kms.KmsMasterKey;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import sfw.example.esdkworkshop.datamodel.BaseItem;
import sfw.example.esdkworkshop.datamodel.ContextItem;
import sfw.example.esdkworkshop.datamodel.DocumentBundle;
import sfw.example.esdkworkshop.datamodel.PointerItem;

public class Api {
  private final AmazonDynamoDBClient ddbClient;
  private final AmazonS3Client s3Client;
  private final AwsCrypto awsEncryptionSDK = new AwsCrypto();
  private final MasterKeyProvider mkp;
  private static final String TABLE_NAME = Config.contents.document_bucket.document_table.name;
  private static final String BUCKET_NAME = Config.contents.document_bucket.bucket.name;

  public Api(AmazonDynamoDBClient ddbClient, AmazonS3Client s3Client, MasterKeyProvider mkp) {
    this.ddbClient = ddbClient;
    this.s3Client = s3Client;
    this.mkp = mkp;
  }

  protected <T extends BaseItem> Map<String, AttributeValue> writeItem(T modeledItem) {
    Map<String, AttributeValue> ddbItem = modeledItem.toItem();
    ddbClient.putItem(TABLE_NAME, ddbItem);
    return ddbItem;
  }

  protected PointerItem getPointerItem(String key) {
    GetItemResult result = ddbClient.getItem(TABLE_NAME, PointerItem.atKey(key));
    PointerItem pointer = PointerItem.fromItem(result.getItem());
    return pointer;
  }

  protected PointerItem getPointerItem(ContextItem contextItem) {
    return getPointerItem(contextItem.sortKey().getS());
  }

  protected Set<PointerItem> queryForContextKey(String contextKey) {
    QueryResult result =
        ddbClient.query(
            new QueryRequest()
                .withTableName(TABLE_NAME)
                .withKeyConditionExpression(ContextItem.queryFor(contextKey)));
    Set<ContextItem> contextItems =
        result.getItems().stream().map(ContextItem::fromItem).collect(Collectors.toSet());
    Set<PointerItem> pointerItems =
        contextItems.stream().map(this::getPointerItem).collect(Collectors.toSet());
    return pointerItems;
  }

  protected void writeObject(DocumentBundle bundle) {
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setUserMetadata(bundle.getPointer().getContext());
    s3Client.putObject(
        BUCKET_NAME,
        bundle.getPointer().partitionKey().getS(),
        new ByteArrayInputStream(bundle.getData()),
        metadata);
  }

  // TODO Return some kind of bundle so that data has been pulled from the stream
  // but metadata is returned as well?
  protected byte[] getObjectData(String key) throws java.io.IOException {
    S3Object object = s3Client.getObject(BUCKET_NAME, key);
    byte[] result;
    try (S3ObjectInputStream stream = object.getObjectContent()) {
      result = IOUtils.toByteArray(stream);
    }
    return result;
  }

  public Set<PointerItem> list() {
    ScanResult result = ddbClient.scan(TABLE_NAME, PointerItem.filterFor());
    Set<PointerItem> mappedItems =
        result.getItems().stream().map(PointerItem::fromItem).collect(Collectors.toSet());
    return mappedItems;
  }

  public DocumentBundle retrieve(String key) throws java.io.IOException {
    return retrieve(key, Collections.emptySet(), Collections.emptyMap());
  }

  public DocumentBundle retrieve(String key, Set<String> expectedContextKeys)
      throws java.io.IOException {
    return retrieve(key, expectedContextKeys, Collections.emptyMap());
  }

  public DocumentBundle retrieve(String key, Map<String, String> expectedContext)
      throws java.io.IOException {
    return retrieve(key, Collections.emptySet(), expectedContext);
  }

  public DocumentBundle retrieve(
      String key, Set<String> expectedContextKeys, Map<String, String> expectedContext)
      throws java.io.IOException {
    PointerItem pointer = getPointerItem(key);
    byte[] data = getObjectData(key);
    CryptoResult<byte[], KmsMasterKey> decryptedMessage = awsEncryptionSDK.decryptData(mkp, data);
    Map<String, String> actualContext = decryptedMessage.getEncryptionContext();
    boolean allExpectedContextKeysFound = actualContext.keySet().containsAll(expectedContextKeys);
    if (!allExpectedContextKeysFound) {
      // Remove all of the keys that were found
      expectedContextKeys.removeAll(actualContext.keySet());
      String error =
          String.format(
              "Expected context keys were not found in the actual encryption context! Missing keys were: %s",
              expectedContextKeys.toString());
      throw new NoSuchElementException(error);
    }
    boolean allExpectedContextFound =
        actualContext.entrySet().containsAll(expectedContext.entrySet());
    if (!allExpectedContextFound) {
      Set<Map.Entry<String, String>> expectedContextEntries = expectedContext.entrySet();
      expectedContextEntries.removeAll(actualContext.entrySet());
      String error =
          String.format(
              "Expected context pairs were not found in the actual encryption context! Missing pairs were: %s",
              expectedContextEntries.toString());
      throw new NoSuchElementException(error);
    }
    return DocumentBundle.fromDataAndPointer(decryptedMessage.getResult(), pointer);
  }

  public PointerItem store(byte[] data) {
    return store(data, Collections.emptyMap());
  }

  public PointerItem store(byte[] data, Map<String, String> context) {
    CryptoResult<byte[], KmsMasterKey> encryptedMessage =
        awsEncryptionSDK.encryptData(mkp, data, context);
    DocumentBundle bundle =
        DocumentBundle.fromDataAndContext(encryptedMessage.getResult(), context);
    writeItem(bundle.getPointer());
    writeObject(bundle);
    return bundle.getPointer();
  }

  public Set<PointerItem> searchByContextKey(String contextKey) {
    return queryForContextKey(contextKey);
  }
}
