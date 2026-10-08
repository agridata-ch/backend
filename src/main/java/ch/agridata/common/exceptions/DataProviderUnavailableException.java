package ch.agridata.common.exceptions;

/**
 * Signals that a data provider could not be called or did not respond in time, e.g. because the maximum number of concurrent
 * requests to the provider is exhausted or the read timeout was exceeded.
 *
 * @CommentLastReviewed 2026-10-02
 */
public class DataProviderUnavailableException extends RuntimeException {

  public DataProviderUnavailableException(String message) {
    super(message);
  }

  public DataProviderUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
