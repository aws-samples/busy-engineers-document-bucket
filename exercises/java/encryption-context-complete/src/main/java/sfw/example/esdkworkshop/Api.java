// CHECKSTYLE:OFF MissingJavadocMethod
// TODO https://github.com/aws-samples/busy-engineers-document-bucket/issues/24

package sfw.example.esdkworkshop;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.MasterKey;
import com.amazonaws.encryptionsdk.MasterKeyProvider;
import com.amazonaws.encryptionsdk.kms.KmsMasterKey;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.s3.AmazonS3;
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
  private final AmazonDynamoDB ddbClient;
  private final AmazonS3 s3Client;
  private final AwsCrypto awsEncryptionSdk;
  private final MasterKeyProvider mkp;
  private final String tableName;
  private final String bucketName;

  public Api(
      AmazonDynamoDB ddbClient,
      String tableName,
      AmazonS3 s3Client,
      String bucketName,
      MasterKeyProvider mkp) {
    this(ddbClient, tableName, s3Client, bucketName, new AwsCrypto(), mkp);
  }

  protected Api(
      AmazonDynamoDB ddbClient,
      String tableName,
      AmazonS3 s3Client,
      String bucketName,
      AwsCrypto awsEncryptionSdk,
      MasterKeyProvider<? extends MasterKey> mkp) {
    this.ddbClient = ddbClient;
    this.tableName = tableName;
    this.s3Client = s3Client;
    this.bucketName = bucketName;
    this.awsEncryptionSdk = awsEncryptionSdk;
    this.mkp = mkp;
  }

  protected <T extends BaseItem> Map<String, AttributeValue> writeItem(T modeledItem) {
    Map<String, AttributeValue> ddbItem = modeledItem.toItem();
    ddbClient.putItem(tableName, ddbItem);
    return ddbItem;
  }

  protected PointerItem getPointerItem(String key) {
    GetItemResult result = ddbClient.getItem(tableName, PointerItem.atKey(key));
    PointerItem pointer = PointerItem.fromItem(result.getItem());
    return pointer;
  }

  protected PointerItem getPointerItem(ContextItem contextItem) {
    return getPointerItem(contextItem.sortKey().getS());
  }

  protected Set<PointerItem> queryForContextKey(String contextKey) {
    QueryResult result = ddbClient.query(ContextItem.queryFor(contextKey).withTableName(tableName));
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
        bucketName,
        bundle.getPointer().partitionKey().getS(),
        new ByteArrayInputStream(bundle.getData()),
        metadata);
  }

  // TODO Return some kind of bundle so that data has been pulled from the stream
  // but metadata is returned as well?
  protected byte[] getObjectData(String key) {
    S3Object object = s3Client.getObject(bucketName, key);
    byte[] result;
    try (S3ObjectInputStream stream = object.getObjectContent()) {
      result = IOUtils.toByteArray(stream);
    } catch (java.io.IOException e) {
      throw new DocumentBucketException("Unable to retrieve object from S3!", e);
    }
    return result;
  }

  public Set<PointerItem> list() {
    ScanResult result = ddbClient.scan(tableName, PointerItem.filterFor());
    Set<PointerItem> mappedItems =
        result.getItems().stream().map(PointerItem::fromItem).collect(Collectors.toSet());
    return mappedItems;
  }

  public DocumentBundle retrieve(String key) {
    return retrieve(key, Collections.emptySet(), Collections.emptyMap());
  }

  public DocumentBundle retrieve(String key, Set<String> expectedContextKeys) {
    return retrieve(key, expectedContextKeys, Collections.emptyMap());
  }

  public DocumentBundle retrieve(String key, Map<String, String> expectedContext) {
    return retrieve(key, Collections.emptySet(), expectedContext);
  }

  public DocumentBundle retrieve(
      String key, Set<String> expectedContextKeys, Map<String, String> expectedContext) {
    PointerItem pointer = getPointerItem(key);
    byte[] data = getObjectData(key);
    CryptoResult<byte[], KmsMasterKey> decryptedMessage = awsEncryptionSdk.decryptData(mkp, data);
    Map<String, String> actualContext = decryptedMessage.getEncryptionContext();
    boolean allExpectedContextKeysFound = actualContext.keySet().containsAll(expectedContextKeys);
    if (!allExpectedContextKeysFound) {
      // Remove all of the keys that were found
      expectedContextKeys.removeAll(actualContext.keySet());
      String error =
          String.format(
              "Expected context keys were not found in the actual encryption context! "
                  + "Missing keys were: %s",
              expectedContextKeys.toString());
      throw new DocumentBucketException(error, new NoSuchElementException());
    }
    boolean allExpectedContextFound =
        actualContext.entrySet().containsAll(expectedContext.entrySet());
    if (!allExpectedContextFound) {
      Set<Map.Entry<String, String>> expectedContextEntries = expectedContext.entrySet();
      expectedContextEntries.removeAll(actualContext.entrySet());
      String error =
          String.format(
              "Expected context pairs were not found in the actual encryption context! "
                  + "Missing pairs were: %s",
              expectedContextEntries.toString());
      throw new DocumentBucketException(error, new NoSuchElementException());
    }
    return DocumentBundle.fromDataAndPointer(decryptedMessage.getResult(), pointer);
  }

  public PointerItem store(byte[] data) {
    return store(data, Collections.emptyMap());
  }

  public PointerItem store(byte[] data, Map<String, String> context) {
    CryptoResult<byte[], KmsMasterKey> encryptedMessage =
        awsEncryptionSdk.encryptData(mkp, data, context);
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
