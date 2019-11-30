package sfw.example.esdkworkshop;

/** Exception to wrap errors encountered during Document Bucket operations. */
public class DocumentBucketException extends RuntimeException {
  public DocumentBucketException(String message, Throwable cause) {
    super(message, cause);
  }
}
