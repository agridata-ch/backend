package ch.agridata.common.exceptions;

/**
 * Represents failures when interacting with external services. It conveys details for troubleshooting integrations.
 *
 * @CommentLastReviewed 2025-08-25
 */
public class ExternalWebServiceException extends RuntimeException {

  public ExternalWebServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExternalWebServiceException(String message) {
    super(message);
  }
}
