package sfw.example.esdkworkshop.datamodel;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.Condition;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class PointerItemTest {

  Map<String, String> sampleContext() {

    Map<String, String> context = new HashMap<>(5);
    context.put("region", "sp-moon-1");
    context.put("user", "kilroy");
    context.put("bananas", "yes");
    context.put("fleet", "gamma");
    context.put("oranges", "glad i didn't say bananas");
    return context;
  }

  @Test
  void testGenerateNoContext() {
    PointerItem item = PointerItem.generate();
    assertEquals(Collections.emptyMap(), item.getContext());
  }

  @Test
  void testGenerateContextHappyCase() {
    Map<String, String> context = sampleContext();
    PointerItem item = PointerItem.generate(context);
    for (Map.Entry entry : item.getContext().entrySet()) {
      assertEquals(context.get(entry.getKey()), entry.getValue());
    }
  }

  @Test
  void testRoundTripHappyCaseNoContext() throws Exception {
    PointerItem expected = PointerItem.generate();
    PointerItem actual = PointerItem.fromItem(expected.toItem());
    assertEquals(expected, actual);
  }

  @Test
  void testRoundTripHappyCaseContext() throws Exception {
    PointerItem expected = PointerItem.generate(sampleContext());
    PointerItem actual = PointerItem.fromItem(expected.toItem());
    assertEquals(expected, actual);
  }

  @Test
  void testFromItemInvalidSortKey() {
    Map<String, AttributeValue> bogusItem = new HashMap<>(2);
    bogusItem.put(PointerItem.partitionKeyName(), new AttributeValue(new UuidKey().toString()));
    bogusItem.put(PointerItem.sortKeyName(), new AttributeValue("bonkers and wrong"));
    assertThrows(
        DataModelException.class,
        () -> {
          PointerItem.fromItem(bogusItem);
        });
  }

  @Test
  void testContextOperationsWhenEmpty() {
    PointerItem item = PointerItem.generate();
    assertEquals(Collections.emptyMap(), item.getContext());
    assertEquals(Collections.emptySet(), item.contextItems());
  }

  @Test
  void testGetContextPopulated() {
    Map<String, String> context = sampleContext();
    PointerItem item = PointerItem.generate(context);
    assertEquals(context, item.getContext());
  }

  @Test
  void testContextItems() {
    Map<String, String> context = sampleContext();
    PointerItem item = PointerItem.generate(context);
    Set<ContextItem> expectedContextItems = new HashSet<>(context.size());
    for (Map.Entry<String, String> entry : context.entrySet()) {
      ContextItem i = ContextItem.fromContext(entry.getKey(), item.partitionKey);
      expectedContextItems.add(i);
    }
    assertEquals(expectedContextItems, item.contextItems());
  }

  @Test
  void testReservedKeysThrow() {
    Map<String, String> partitionKeyContext = new HashMap<>(1);
    partitionKeyContext.put(PointerItem.partitionKeyName(), "kaboom");
    Map<String, String> sortKeyContext = new HashMap<>(1);
    sortKeyContext.put(PointerItem.sortKeyName(), "kaboom");
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          PointerItem.generate(partitionKeyContext);
        });
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          PointerItem.generate(sortKeyContext);
        });
  }

  @Test
  void testAtKeyWithBogusKeyThrows() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          PointerItem.atKey("blammo");
        });
  }

  @Test
  void testAtKeyWithStringHappyCase() {
    PointerItem test = PointerItem.generate();
    Map<String, AttributeValue> actual = PointerItem.atKey(test.partitionKey().getS());
    assertEquals(test.toItem(), actual);
  }

  @Test
  void testAtKeyHappyCase() {
    // No encryption context
    PointerItem test = PointerItem.generate();
    Map<String, AttributeValue> actual = PointerItem.atKey(test.partitionKey());
    assertEquals(test.toItem(), actual);
  }

  @Test
  void testFilterFor() {
    Map<String, Condition> filter = PointerItem.filterFor();
    Condition filterCondition = filter.get(PointerItem.sortKeyName());
    assertNotNull(filterCondition);
    assertTrue(filterCondition.toString().contains(PointerItem.TARGET));
  }

  @Test
  void testToStringSanity() {
    PointerItem test = PointerItem.generate(sampleContext());
    for (Map.Entry<String, String> entry : test.getContext().entrySet()) {
      assertTrue(test.toString().contains(entry.getKey()));
      assertTrue(test.toString().contains(entry.getValue()));
    }
  }
}
