package ch.agridata.common.exceptions;

/**
 * Represents failures when interacting with external services that provide basic UID authorization.
 * It conveys details for troubleshooting integrations.
 *
 * @CommentLastReviewed 2026-04-14
 */
public class UidProviderUnavailableException extends ExternalWebServiceException {

  public UidProviderUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }

  public UidProviderUnavailableException(String message) {
    super(message);
  }
}
