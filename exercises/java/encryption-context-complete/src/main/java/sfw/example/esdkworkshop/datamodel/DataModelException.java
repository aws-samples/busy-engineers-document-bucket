// CHECKSTYLE:OFF MissingJavadocMethod
// TODO https://github.com/aws-samples/busy-engineers-document-bucket/issues/24

package sfw.example.esdkworkshop.datamodel;

public class DataModelException extends RuntimeException {
  static final long serialVersionUID = 1L;

  public DataModelException(String msg) {
    super(msg);
  }
}
