// CHECKSTYLE:OFF MissingJavadocMethod
// TODO https://github.com/aws-samples/busy-engineers-document-bucket/issues/24
package sfw.example.esdkworkshop.datamodel;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import sfw.example.esdkworkshop.Config;

public abstract class BaseItem {
  protected final AttributeValue partitionKey;
  protected final AttributeValue sortKey;
  private static final String PARTITION_KEY_NAME =
      Config.contents.document_bucket.document_table.partition_key;
  private static final String SORT_KEY_NAME =
      Config.contents.document_bucket.document_table.sort_key;

  BaseItem(String partitionKey, String sortKey) {
    this.partitionKey = new AttributeValue(partitionKey);
    this.sortKey = new AttributeValue(sortKey);
  }

  public Map<String, AttributeValue> toItem() {
    Map<String, AttributeValue> item = new HashMap<>();
    item.put(PARTITION_KEY_NAME, partitionKey);
    item.put(SORT_KEY_NAME, sortKey);
    return item;
  }

  public static String partitionKeyName() {
    return PARTITION_KEY_NAME;
  }

  public static String sortKeyName() {
    return SORT_KEY_NAME;
  }

  public AttributeValue partitionKey() {
    return this.partitionKey;
  }

  public AttributeValue sortKey() {
    return this.sortKey;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (this.getClass().equals(other.getClass())) {
      BaseItem i = (BaseItem) other;
      return this.partitionKey.equals(i.partitionKey) && this.sortKey.equals(i.sortKey);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(partitionKey, sortKey);
  }

  @Override
  public String toString() {
    return this.toItem().toString();
  }
}
