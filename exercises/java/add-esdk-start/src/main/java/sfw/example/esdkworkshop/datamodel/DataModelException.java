package sfw.example.esdkworkshop.datamodel;

/** A wrapper exception for problems with data model operations. */
public class DataModelException extends RuntimeException {
  static final long serialVersionUID = 1L;

  public DataModelException(String msg) {
    super(msg);
  }
}
