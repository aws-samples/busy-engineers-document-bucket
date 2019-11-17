// CHECKSTYLE:OFF MissingJavadocMethod
// TODO https://github.com/aws-samples/busy-engineers-document-bucket/issues/24

package sfw.example.esdkworkshop;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import sfw.example.esdkworkshop.datamodel.DocumentBundle;
import sfw.example.esdkworkshop.datamodel.PointerItem;
import sfw.example.esdkworkshop.datamodel.UuidKey;

public class Api {

  public Set<PointerItem> list() {
    return null;
  }

  public DocumentBundle retrieve(UuidKey key) {
    return null;
  }

  public PointerItem store(byte[] data) {
    return store(data, Collections.emptyMap());
  }

  public PointerItem store(byte[] data, Map<String, String> context) {
    return null;
  }

  public Set<PointerItem> searchByContextKey(String contextKey) {
    return null;
  }
}
