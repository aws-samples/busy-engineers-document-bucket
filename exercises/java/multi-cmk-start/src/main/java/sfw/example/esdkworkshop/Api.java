// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

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
import java.util.Set;
import java.util.stream.Collectors;
import sfw.example.esdkworkshop.datamodel.BaseItem;
import sfw.example.esdkworkshop.datamodel.ContextItem;
import sfw.example.esdkworkshop.datamodel.DocumentBundle;
import sfw.example.esdkworkshop.datamodel.PointerItem;

/** Defines the public interface to the Document Bucket operations. */
public class Api {
  private final AmazonDynamoDB ddbClient;
  private final AmazonS3 s3Client;
  private final AwsCrypto awsEncryptionSdk;
  private final MasterKeyProvider mkp;
  private final String tableName;
  private final String bucketName;

  /**
   * Construct a Document Bucket {@code Api} using a default {@link AwsCrypto} instance.
   *
   * @param ddbClient the {@link AmazonDynamoDB} to use to interact with Amazon DynamoDB.
   * @param tableName the name of the Document Bucket table.
   * @param s3Client the {@link AmazonS3} to use to interact with Amazon S3.
   * @param bucketName the name of the Document Bucket, err, bucket.
   * @param mkp the {@link MasterKeyProvider} to use for Encryption and Decryption operations with
   *     {@link AwsCrypto}.
   */
  public Api(
      AmazonDynamoDB ddbClient,
      String tableName,
      AmazonS3 s3Client,
      String bucketName,
      MasterKeyProvider mkp) {
    this(ddbClient, tableName, s3Client, bucketName, new AwsCrypto(), mkp);
  }

  /**
   * Construct a Document Bucket {@code Api} using the provided {@link AwsCrypto} instance.
   * (Included to facilitate unit testing.)
   *
   * @param ddbClient the {@link AmazonDynamoDB} to use to interact with Amazon DynamoDB.
   * @param tableName the name of the Document Bucket table.
   * @param s3Client the {@link AmazonS3} to use to interact with Amazon S3.
   * @param bucketName the name of the Document Bucket, err, bucket.
   * @param awsEncryptionSdk the {@link AwsCrypto} instance to use for Encryption and Decryption
   *     operations.
   * @param mkp the {@link MasterKeyProvider} to use for Encryption and Decryption operations with
   *     {@link AwsCrypto}.
   */
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

  /**
   * Writes a {@link BaseItem} item to the DynamoDB table.
   *
   * @param modeledItem the item to write.
   * @param <T> the subtype of item to write.
   * @return the actual item value written ({@link Map} of {@link String}:{@link AttributeValue}).
   */
  protected <T extends BaseItem> Map<String, AttributeValue> writeItem(T modeledItem) {
    Map<String, AttributeValue> ddbItem = modeledItem.toItem();
    ddbClient.putItem(tableName, ddbItem);
    return ddbItem;
  }

  /**
   * Retrieves a {@link PointerItem} for the supplied key.
   *
   * @param key the key for which to fetch the {@link PointerItem}.
   * @return the {@link PointerItem} found.
   */
  protected PointerItem getPointerItem(String key) {
    GetItemResult result = ddbClient.getItem(tableName, PointerItem.atKey(key));
    PointerItem pointer = PointerItem.fromItem(result.getItem());
    return pointer;
  }

  /**
   * Retrieves the {@link PointerItem} for an associated {@link ContextItem}-pointer pair.
   *
   * @param contextItem the {@link ContextItem} with the desired document pointer key.
   * @return the {@link PointerItem} for that context and pointer pair.
   */
  protected PointerItem getPointerItem(ContextItem contextItem) {
    return getPointerItem(contextItem.sortKey().getS());
  }

  /**
   * Query DynamoDB for the records associated with the supplied context key.
   *
   * @param contextKey the key for which to retrieve the list of matching records.
   * @return the {@link Set} of {@link PointerItem}s that have that context key.
   */
  protected Set<PointerItem> queryForContextKey(String contextKey) {
    QueryResult result = ddbClient.query(ContextItem.queryFor(contextKey).withTableName(tableName));
    Set<ContextItem> contextItems =
        result.getItems().stream().map(ContextItem::fromItem).collect(Collectors.toSet());
    Set<PointerItem> pointerItems =
        contextItems.stream().map(this::getPointerItem).collect(Collectors.toSet());
    return pointerItems;
  }

  /**
   * Helper to perform the write operations required to store the provided {@link DocumentBundle} in
   * the Document Bucket system.
   *
   * @param bundle the document to store.
   */
  protected void writeObject(DocumentBundle bundle) {
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setUserMetadata(bundle.getPointer().getContext());
    s3Client.putObject(
        bucketName,
        bundle.getPointer().partitionKey().getS(),
        new ByteArrayInputStream(bundle.getData()),
        metadata);
  }

  /**
   * Retrieve the bytes associated with the key in S3.
   *
   * @param key the S3 key to retrieve.
   * @return the bytes for that key.
   */
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

  /**
   * Lists all of the Document Bucket {@link PointerItem}s in the DynamoDB table.
   *
   * <p>These correspond to documents in the Document Bucket.
   *
   * @return the {@link Set} of {@link PointerItem}s in the Document Bucket.
   */
  public Set<PointerItem> list() {
    ScanResult result = ddbClient.scan(tableName, PointerItem.filterFor());
    Set<PointerItem> mappedItems =
        result.getItems().stream().map(PointerItem::fromItem).collect(Collectors.toSet());
    return mappedItems;
  }

  /**
   * Stores the supplied Data as a new document in the Document Bucket.
   *
   * @param data the data to store.
   * @return the {@link PointerItem} under which this data is stored.
   */
  public PointerItem store(byte[] data) {
    return store(data, Collections.emptyMap());
  }

  /**
   * Stores the supplied Data as a new document in the Document Bucket, along with the supplied
   * Context.
   *
   * @param data the data to store.
   * @param context the context for this data.
   * @return the {@link PointerItem} under which this data and context are stored.
   */
  public PointerItem store(byte[] data, Map<String, String> context) {
    CryptoResult<byte[], KmsMasterKey> encryptedMessage = awsEncryptionSdk.encryptData(mkp, data);
    DocumentBundle bundle =
        DocumentBundle.fromDataAndContext(encryptedMessage.getResult(), context);
    writeItem(bundle.getPointer());
    writeObject(bundle);
    return bundle.getPointer();
  }

  /**
   * Retrieves a document at the provided key.
   *
   * @param key the key under which the document and its metadata are stored.
   * @return the {@link DocumentBundle} containing the document data and its metadata.
   */
  public DocumentBundle retrieve(String key) {
    return retrieve(key, Collections.emptySet(), Collections.emptyMap());
  }

  /**
   * Retrieves a document at the provided key.
   *
   * @param key the key under which the document and its metadata are stored.
   * @param expectedContextKeys TODO do something with this argument. :)
   * @return the {@link DocumentBundle} containing the document data and its metadata.
   */
  public DocumentBundle retrieve(String key, Set<String> expectedContextKeys) {
    return retrieve(key, expectedContextKeys, Collections.emptyMap());
  }

  /**
   * Retrieves a document at the provided key.
   *
   * @param key the key under which the document and its metadata are stored.
   * @param expectedContext TODO do something with this argument. :)
   * @return the {@link DocumentBundle} containing the document data and its metadata.
   */
  public DocumentBundle retrieve(String key, Map<String, String> expectedContext) {
    return retrieve(key, Collections.emptySet(), expectedContext);
  }

  /**
   * Retrieves a document at the provided key.
   *
   * @param key the key under which the document and its metadata are stored.
   * @param expectedContextKeys TODO do something with this argument. :)
   * @param expectedContext TODO do something with this argument. :)
   * @return the {@link DocumentBundle} containing the document data and its metadata.
   */
  public DocumentBundle retrieve(
      String key, Set<String> expectedContextKeys, Map<String, String> expectedContext) {
    byte[] data = getObjectData(key);
    CryptoResult<byte[], KmsMasterKey> decryptedMessage = awsEncryptionSdk.decryptData(mkp, data);
    PointerItem pointer = getPointerItem(key);
    return DocumentBundle.fromDataAndPointer(decryptedMessage.getResult(), pointer);
  }

  /**
   * Search the Document Bucket for any documents that have context with the supplied key.
   *
   * @param contextKey the key for which to search for matching documents.
   * @return the {@link Set} of {@link PointerItem}s for matching documents.
   */
  public Set<PointerItem> searchByContextKey(String contextKey) {
    return queryForContextKey(contextKey);
  }
}
