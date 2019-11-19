package sfw.example.esdkworkshop.datamodel;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.junit.jupiter.api.Test;

public class BaseItemTest {

  @Test
  void testEqualsDiffTypes() {
    PointerItem item = PointerItem.generate();
    assertNotEquals(item, new Object());
  }

  @Test
  void testEqualsDifferentSorts() {
    String contextKey = "c00l";
    ContextItem item1 = ContextItem.fromContext(contextKey, new UuidKey());
    ContextItem item2 = ContextItem.fromContext(contextKey, new UuidKey());
    assertNotEquals(item1, item2);
  }

  @Test
  void testEqualsSameKeysDifferentKeyTypes() {
    UuidKey objectKey = new UuidKey();
    String contextKey = "aw350m3";
    ContextItem item1 = ContextItem.fromContext(contextKey, objectKey);
    ContextItem item2 =
        ContextItem.fromContext(contextKey, new AttributeValue(objectKey.toString()));
    assertEquals(item1, item2);
  }

  @Test
  void testEqualsDifferentPartitionSameSort() {
    UuidKey objectKey = new UuidKey();
    String contextKey1 = "tubular";
    String contextKey2 = "radical";
    assertNotEquals(
        ContextItem.fromContext(contextKey1, objectKey),
        ContextItem.fromContext(contextKey2, objectKey));
  }

  @Test
  void testEqualsWithNull() {
    assertFalse(PointerItem.generate().equals(null));
  }

  @Test
  void testKeyGetters() {
    PointerItem test = PointerItem.generate();
    assertEquals(test.partitionKey(), test.partitionKey);
    assertEquals(test.sortKey(), test.sortKey);
  }
}
