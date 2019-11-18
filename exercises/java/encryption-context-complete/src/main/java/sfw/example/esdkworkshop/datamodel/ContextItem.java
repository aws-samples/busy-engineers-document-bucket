// CHECKSTYLE:OFF MissingJavadocMethod
// TODO https://github.com/aws-samples/busy-engineers-document-bucket/issues/24

package sfw.example.esdkworkshop.datamodel;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import java.util.Map;

public class ContextItem extends BaseItem {
  // FIXME TOML Plumbing https://github.com/aws-samples/busy-engineers-document-bucket/issues/19
  protected static final String PREFIX = "CONTEXT_";

  protected ContextItem(String contextKey, UuidKey objectTarget) {
    super(contextKey, objectTarget.toString());
  }

  protected static String canonicalize(String key) {
    if (key.startsWith(PREFIX)) {
      return key;
    }
    return PREFIX + key;
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
