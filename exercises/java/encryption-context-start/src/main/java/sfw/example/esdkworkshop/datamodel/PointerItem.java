package sfw.example.esdkworkshop.datamodel;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import sfw.example.esdkworkshop.Config;

/**
 * Modeled item corresponding to a Document Bucket pointer item. Pointer Items are DynamoDB items
 * that maintain records of document items in the Document Bucket S3 bucket, and their associated
 * metadata.
 */
public class PointerItem extends BaseItem {
  protected static final String TARGET =
      Config.contents.document_bucket.document_table.object_target;
  protected static final AttributeValue ATTR_TARGET = new AttributeValue(TARGET);
  private final Map<String, AttributeValue> context;

  protected PointerItem(UuidKey pointerTarget, Map<String, AttributeValue> context) {
    super(pointerTarget.toString(), TARGET);
    if (context.keySet().contains(PointerItem.partitionKeyName())
        || context.keySet().contains(PointerItem.sortKeyName())) {
      String err =
          String.format(
              "Can't create an encryption context with reserved key %s or %s",
              PointerItem.partitionKeyName(), PointerItem.sortKeyName());
      throw new IllegalArgumentException(err);
    }
    this.context = context;
  }

  /**
   * Helper to provide a DynamoDB condition filter to filter for only {@link PointerItem} records
   * during scans.
   *
   * @return the filter for the scan operation.
   */
  public static Map<String, Condition> filterFor() {
    Map<String, Condition> result = new HashMap<>(1);
    result.put(
        sortKeyName(),
        new Condition()
            .withAttributeValueList(ATTR_TARGET)
            .withComparisonOperator(ComparisonOperator.EQ));
    return result;
  }

  /**
   * Create a new pointer for a new Document Bucket document, with no associated context.
   *
   * @return a new {@link PointerItem} for a new document record.
   */
  public static PointerItem generate() {
    return generate(Collections.emptyMap());
  }

  /**
   * Create a new pointer for a Document Bucket document with the provided context.
   *
   * @param context the context for this document.
   * @return a new {@link PointerItem} for a new document record.
   */
  public static PointerItem generate(Map<String, String> context) {
    return fromKeyAndContext(new UuidKey().toString(), context);
  }

  /**
   * Return a modeled {@link PointerItem} representing the associated pointer key and context.
   *
   * @param key the pointer key for this item.
   * @param context the context for this document record.
   * @return the {@link PointerItem} for this record.
   */
  public static PointerItem fromKeyAndContext(String key, Map<String, String> context) {
    Map<String, AttributeValue> attributeContext = new HashMap<>(context.size());

    for (Map.Entry<String, String> entry : context.entrySet()) {
      attributeContext.put(entry.getKey(), new AttributeValue(entry.getValue()));
    }

    return new PointerItem(new UuidKey(key), attributeContext);
  }

  /**
   * Retrieve the context for this document item that this record points to.
   *
   * @return the context for this pointer's document.
   */
  public Map<String, String> getContext() {
    Map<String, String> result = new HashMap<>(context.size());
    for (Map.Entry<String, AttributeValue> entry : context.entrySet()) {
      result.put(entry.getKey(), entry.getValue().getS());
    }
    return Collections.unmodifiableMap(result);
  }

  @Override
  public Map<String, AttributeValue> toItem() {
    Map<String, AttributeValue> result = super.toItem();
    result.putAll(context);
    return result;
  }

  /**
   * Transform the provided pointer record key into a DynamoDB key item. This provides a DynamoDB
   * item that has the partition key and sort key populated.
   *
   * @param key the key to transform.
   * @return a DynamoDB-formatted {@link PointerItem} for this key.
   */
  public static Map<String, AttributeValue> atKey(String key) {
    new UuidKey(key); // Check validity
    return atKey(new AttributeValue(key));
  }

  /**
   * Transform the provided pointer record key into a DynamoDB key item. This provides a DynamoDB
   * item that has the partition key and sort key populated.
   *
   * @param key the key to transform.
   * @return a DynamoDB-formatted {@link PointerItem} for this key.
   */
  public static Map<String, AttributeValue> atKey(AttributeValue key) {
    Map<String, AttributeValue> result = new HashMap<>(2);
    result.put(partitionKeyName(), key);
    result.put(sortKeyName(), ATTR_TARGET);
    return result;
  }

  /**
   * Helper function to transform a DynamoDB item into a modeled {@link PointerItem}.
   *
   * @param item the modeled {@link PointerItem}.
   * @return a {@link PointerItem} for the provided item contents.
   */
  public static PointerItem fromItem(Map<String, AttributeValue> item) {
    UuidKey partitionKey = new UuidKey(item.remove(partitionKeyName()).getS());
    String sortKey = item.remove(sortKeyName()).getS();
    if (!sortKey.equals(TARGET)) {
      throw new DataModelException(
          String.format("Unexpected sortKey value (%s) for PointerItem!", sortKey));
    }
    return new PointerItem(partitionKey, item);
  }

  /**
   * Retrieve the set of DynamoDB items for this pointer record's context keys.
   *
   * @return a {@link Set} of {@link ContextItem}s for each key in this pointer's context.
   */
  public Set<ContextItem> contextItems() {
    HashSet<ContextItem> contextItems = new HashSet<>(context.size());
    for (Map.Entry<String, AttributeValue> entry : context.entrySet()) {
      contextItems.add(ContextItem.fromContext(entry.getKey(), partitionKey));
    }
    return contextItems;
  }
}
