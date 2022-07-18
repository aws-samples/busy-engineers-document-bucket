// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package sfw.example.esdkworkshop.datamodel;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * A Document Bucket document. Bundles context metadata and the data itself into a modeled object.
 */
public class DocumentBundle {
  private final PointerItem pointer;
  private final byte[] data;

  DocumentBundle(byte[] data, PointerItem pointer) {
    this.pointer = pointer;
    this.data = Arrays.copyOf(data, data.length);
  }

  /**
   * Construct a new Document Bucket bundle from the provided data.
   *
   * @param data the data for this bundle.
   * @return a new {@link DocumentBundle} for storage in the Document Bucket.
   */
  public static DocumentBundle fromData(byte[] data) {
    return fromDataAndContext(data, Collections.emptyMap());
  }

  /**
   * Construct a new Document Bucket bundle from the provided data and pointer record.
   *
   * @param data the data for this bundle.
   * @param pointer the item that tracks this record in the Document Bucket database.
   * @return a new {@link DocumentBundle} for storage in the Document Bucket.
   */
  public static DocumentBundle fromDataAndPointer(byte[] data, PointerItem pointer) {
    return new DocumentBundle(data, pointer);
  }

  /**
   * Construct a new Document Bucket bundle from the provided data and context.
   *
   * @param data the data for this bundle.
   * @param context the context for this document bundle.
   * @return a new {@link DocumentBundle} for storage in the Document Bucket.
   */
  public static DocumentBundle fromDataAndContext(byte[] data, Map<String, String> context) {
    return new DocumentBundle(data, PointerItem.generate(context));
  }

  /**
   * Get the data for this {@link DocumentBundle}.
   *
   * @return the associated data.
   */
  public byte[] getData() {
    return Arrays.copyOf(data, data.length);
  }

  /**
   * Get the {@link PointerItem} for this Document Bucket bundle.
   *
   * @return the associated {@link PointerItem}.
   */
  public PointerItem getPointer() {
    return pointer;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder(pointer.toString());
    b.append(System.getProperty("line.separator"));
    b.append("Data: {");
    for (int i = 0; i < data.length; i++) {
      b.append(String.format("%X", data[i]));
    }
    b.append("}");
    return b.toString();
  }
}
