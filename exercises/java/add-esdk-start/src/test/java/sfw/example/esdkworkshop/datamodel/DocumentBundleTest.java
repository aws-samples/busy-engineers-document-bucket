package sfw.example.esdkworkshop.datamodel;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DocumentBundleTest {

  Map<String, String> standardContext() {
    Map<String, String> context = new HashMap<>(5);
    context.put("foo", "bar");
    context.put("region", "sp-moon-1");
    context.put("fleet", "shiny");
    context.put("shard", "crystal");
    context.put("metatype", "dwarf");
    return context;
  }

  @Test
  void testConstructorHappyCase() {
    byte[] sampleData = {(byte) 0xDE, (byte) 0xCA, (byte) 0xFB, (byte) 0xAD};
    DocumentBundle bundle = DocumentBundle.fromData(sampleData);
    assertArrayEquals(sampleData, bundle.getData());
    assertNotNull(bundle.getPointer());
  }

  @Test
  void testConstructorSanity() {
    byte[] sampleData = {(byte) 0xDE, (byte) 0xCA, (byte) 0xFB, (byte) 0xAD};
    PointerItem pointer = PointerItem.generate();
    DocumentBundle bundle = new DocumentBundle(sampleData, pointer);
    assertEquals(pointer, bundle.getPointer());
    assertArrayEquals(sampleData, bundle.getData());
  }

  @Test
  void testFactoryWithPointer() {
    byte[] sampleData = {
      (byte) 0xC0, (byte) 0xFF, (byte) 0xEE, (byte) 0xCA, (byte) 0xB0, (byte) 0x05, (byte) 0xE5
    };
    PointerItem pointer = PointerItem.generate(standardContext());
    DocumentBundle bundle = DocumentBundle.fromDataAndPointer(sampleData, pointer);
    assertEquals(pointer, bundle.getPointer());
    assertArrayEquals(sampleData, bundle.getData());
  }

  @Test
  void testToStringSanity() {

    byte[] sampleData = {
      (byte) 0xC0, (byte) 0xFF, (byte) 0xEE, (byte) 0xCA, (byte) 0xB0, (byte) 0x05, (byte) 0xE5
    };
    PointerItem pointer = PointerItem.generate(standardContext());
    DocumentBundle bundle = DocumentBundle.fromDataAndPointer(sampleData, pointer);
    assertTrue(bundle.toString().contains(pointer.toString()));
    for (int i = 0; i < sampleData.length; i++) {
      assertTrue(bundle.toString().contains(String.format("%X", sampleData[i])));
    }
  }
}
