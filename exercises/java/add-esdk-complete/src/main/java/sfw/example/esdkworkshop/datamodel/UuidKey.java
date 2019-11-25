// CHECKSTYLE:OFF MissingJavadocMethod
// TODO https://github.com/aws-samples/busy-engineers-document-bucket/issues/24

package sfw.example.esdkworkshop.datamodel;

import java.util.UUID;

public class UuidKey {
  private final UUID key;

  protected UuidKey() {
    key = UUID.randomUUID();
  }

  protected UuidKey(String uuid) {
    key = UUID.fromString(uuid);
  }

  @Override
  public String toString() {
    return key.toString();
  }
}
