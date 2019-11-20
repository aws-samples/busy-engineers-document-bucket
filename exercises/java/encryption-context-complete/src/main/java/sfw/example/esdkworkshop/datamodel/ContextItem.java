// CHECKSTYLE:OFF MissingJavadocMethod
// TODO https://github.com/aws-samples/busy-engineers-document-bucket/issues/24

package sfw.example.esdkworkshop.datamodel;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import java.util.Map;
import sfw.example.esdkworkshop.Config;

public class ContextItem extends BaseItem {
  protected static final String PREFIX = Config.contents.document_bucket.document_table.ctx_prefix;

  protected ContextItem(String contextKey, UuidKey objectTarget) {
    super(contextKey, objectTarget.toString());
  }

  protected static String canonicalize(String key) {
    if (key.startsWith(PREFIX)) {
      return key;
    }
    return PREFIX + key;
  }

  public static QueryRequest queryFor(String contextKey) {
    Condition keyIsContextKey =
        new Condition()
            .withAttributeValueList(new AttributeValue(canonicalize(contextKey)))
            .withComparisonOperator(ComparisonOperator.EQ);
    QueryRequest query =
        new QueryRequest().addKeyConditionsEntry(partitionKeyName(), keyIsContextKey);
    return query;
  }

  public static ContextItem fromContext(String key, String objectTarget) {
    UuidKey target = new UuidKey(objectTarget);
    return new ContextItem(canonicalize(key), target);
  }

  public static ContextItem fromContext(String key, UuidKey objectTarget) {
    return new ContextItem(canonicalize(key), objectTarget);
  }

  public static ContextItem fromContext(String key, AttributeValue objectTarget) {
    return fromContext(key, objectTarget.getS());
  }

  public static ContextItem fromItem(Map<String, AttributeValue> item) {
    String contextKey = item.get(partitionKeyName()).getS();
    String objectTarget = item.get(sortKeyName()).getS();
    return fromContext(contextKey, objectTarget);
  }
}
