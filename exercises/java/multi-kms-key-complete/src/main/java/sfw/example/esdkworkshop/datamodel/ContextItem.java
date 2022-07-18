// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package sfw.example.esdkworkshop.datamodel;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import java.util.Map;
import sfw.example.esdkworkshop.Config;

/**
 * Modeled item corresponding to a Document Bucket context item. Context Items are DynamoDB items
 * that maintain lists of which Document Bucket items have which metadata keys.
 */
public class ContextItem extends BaseItem {
  protected static final String PREFIX = Config.contents.document_bucket.document_table.ctx_prefix;

  protected ContextItem(String contextKey, UuidKey objectTarget) {
    super(contextKey, objectTarget.toString());
  }

  /**
   * Ensure that the provided key is in canonical form as a {@code ContextItem} partition key.
   *
   * @param key the key to canonicalize (if needed).
   * @return a canonicalized {@code key}, if it was not already in canonical form.
   */
  protected static String canonicalize(String key) {
    if (key.startsWith(PREFIX)) {
      return key;
    }
    return PREFIX + key;
  }

  /**
   * Helper to build a DynamoDB {@link QueryRequest} for the provided context key. This query will
   * return pointer records that have this context key in their context.
   *
   * @param contextKey the context key to search for.
   * @return the {@link QueryRequest} to find matching records in DynamoDB.
   */
  public static QueryRequest queryFor(String contextKey) {
    Condition keyIsContextKey =
        new Condition()
            .withAttributeValueList(new AttributeValue(canonicalize(contextKey)))
            .withComparisonOperator(ComparisonOperator.EQ);
    QueryRequest query =
        new QueryRequest().addKeyConditionsEntry(partitionKeyName(), keyIsContextKey);
    return query;
  }

  /**
   * Return a new {@link ContextItem} from the provided key and target.
   *
   * @param key the context key for which to associate a new item.
   * @param objectTarget the pointer key that has this context key.
   * @return a new {@link ContextItem} for that context key and pointer target.
   */
  public static ContextItem fromContext(String key, String objectTarget) {
    UuidKey target = new UuidKey(objectTarget);
    return new ContextItem(canonicalize(key), target);
  }

  /**
   * Return a new {@link ContextItem} from the provided key and target.
   *
   * @param key the context key for which to associate a new item.
   * @param objectTarget the pointer key that has this context key.
   * @return a new {@link ContextItem} for that context key and pointer target.
   */
  public static ContextItem fromContext(String key, UuidKey objectTarget) {
    return new ContextItem(canonicalize(key), objectTarget);
  }

  /**
   * Return a new {@link ContextItem} from the provided key and target.
   *
   * @param key the context key for which to associate a new item.
   * @param objectTarget the pointer key that has this context key.
   * @return a new {@link ContextItem} for that context key and pointer target.
   */
  public static ContextItem fromContext(String key, AttributeValue objectTarget) {
    return fromContext(key, objectTarget.getS());
  }

  /**
   * Helper function to transform a DynamoDB item into a modeled {@link ContextItem}.
   *
   * @param item the modeled {@link ContextItem}.
   * @return a {@link ContextItem} for the provided item contents.
   */
  public static ContextItem fromItem(Map<String, AttributeValue> item) {
    String contextKey = item.get(partitionKeyName()).getS();
    String objectTarget = item.get(sortKeyName()).getS();
    return fromContext(contextKey, objectTarget);
  }
}
