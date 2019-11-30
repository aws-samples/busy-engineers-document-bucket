package sfw.example.esdkworkshop.datamodel;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import sfw.example.esdkworkshop.Config;

/**
 * Parent class for modeling items for the DocumentBucket DynamoDB table. See {@link ContextItem}
 * for items corresponding to key records for indexing items with a context key. See {@link
 * PointerItem} for items corresponding to pointer records for documents.
 */
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

  /**
   * Transform this modeled item into a DynamoDB item ready to write to the table.
   *
   * @return the item in {@link Map} of ({@link String}, {@link AttributeValue}) pairs, ready to
   *     write.
   */
  public Map<String, AttributeValue> toItem() {
    Map<String, AttributeValue> item = new HashMap<>();
    item.put(PARTITION_KEY_NAME, partitionKey);
    item.put(SORT_KEY_NAME, sortKey);
    return item;
  }

  /**
   * Return the name of the item attribute used as partition key.
   *
   * @return the partition key item attribute name.
   */
  public static String partitionKeyName() {
    return PARTITION_KEY_NAME;
  }

  /**
   * Return the name of the item attribute used as sort key.
   *
   * @return the sort key item attribute name.
   */
  public static String sortKeyName() {
    return SORT_KEY_NAME;
  }

  /**
   * Return the value of this item's partition key attribute.
   *
   * @return the value of the partition key attribute.
   */
  public AttributeValue partitionKey() {
    return this.partitionKey;
  }

  /**
   * Return the value of this item's sort key attribute.
   *
   * @return the value of the sort key attribute.
   */
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
