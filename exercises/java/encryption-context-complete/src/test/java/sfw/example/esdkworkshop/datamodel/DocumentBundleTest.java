package sfw.example.esdkworkshop.datamodel;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class DocumentBundleTest {

  @Test
  void testConstructorHappyCase() {
    byte[] sampleData = {(byte) 0xDE, (byte) 0xCA, (byte) 0xFB, (byte) 0xAD};
    DocumentBundle bundle = DocumentBundle.fromData(sampleData);
    assertTrue(Arrays.equals(sampleData, bundle.getData()));
    assertNotNull(bundle.getPointer());
  }

  @Test
  void testConstructorSanity() {
    byte[] sampleData = {(byte) 0xDE, (byte) 0xCA, (byte) 0xFB, (byte) 0xAD};
    PointerItem pointer = PointerItem.generate();
    DocumentBundle bundle = new DocumentBundle(sampleData, pointer);
    assertEquals(pointer, bundle.getPointer());
    assertTrue(Arrays.equals(sampleData, bundle.getData()));
  }
}
