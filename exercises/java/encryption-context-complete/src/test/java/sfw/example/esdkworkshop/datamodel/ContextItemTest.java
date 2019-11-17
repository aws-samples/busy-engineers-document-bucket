package sfw.example.esdkworkshop.datamodel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ContextItemTest {
  @Test
  void testCanonicalizeHappyCase() {
    String test = "coolkey";
    String expected = ContextItem.PREFIX + test;
    assertEquals(expected, ContextItem.canonicalize(test));
  }

  @Test
  void testCanonicalizeAlreadyPrefixed() {
    String expected = ContextItem.PREFIX + "BATS";
    assertEquals(expected, ContextItem.canonicalize(expected));
  }

  @Test
  void testDifferentKeyCapitalizationNotEqual() {
    UuidKey key = new UuidKey();
    String test1 = "sp-m00n-1";
    String test2 = "SP-M00N-1";
    ContextItem context1 = ContextItem.fromContext(test1, key);
    ContextItem context2 = ContextItem.fromContext(test2, key);
    assertNotEquals(context1, context2);
    assertNotEquals(context1.partitionKey, context2.partitionKey);
  }

  @Test
  void testBogusTargetKeyThrows() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          ContextItem.fromContext("happy", "garbageTarget");
        });
  }

  @Test
  void fromContextHappyCaseAllStrings() {
    PointerItem validPointer = PointerItem.generate();
    String contextKey = ContextItem.canonicalize("importantContext");
    ContextItem item = ContextItem.fromContext(contextKey, validPointer.partitionKey.getS());
    assertEquals(contextKey, item.partitionKey.getS());
    assertEquals(validPointer.partitionKey.getS(), item.sortKey.getS());
  }

  @Test
  void fromContextHappyCaseUuidKey() {
    UuidKey key = new UuidKey();
    String contextKey = ContextItem.canonicalize("alsoImportantContext");
    ContextItem item = ContextItem.fromContext(contextKey, key);
    assertEquals(contextKey, item.partitionKey.getS());
    assertEquals(key.toString(), item.sortKey.getS());
  }

  @Test
  void fromContextHappyCaseHappyCaseAttributeValue() {
    PointerItem validPointer = PointerItem.generate();
    String contextKey = ContextItem.canonicalize("veryImportantContext");
    ContextItem item = ContextItem.fromContext(contextKey, validPointer.partitionKey);
    assertEquals(contextKey, item.partitionKey.getS());
    assertEquals(validPointer.partitionKey, item.sortKey);
  }

  @Test
  void fromItemHappyCase() {
    PointerItem validPointer = PointerItem.generate();
    String contextKey = ContextItem.canonicalize("superImportantContext");
    ContextItem item = ContextItem.fromContext(contextKey, validPointer.partitionKey);
    ContextItem roundTrip = ContextItem.fromItem(item.toItem());
    assertEquals(item, roundTrip);
  }
}
