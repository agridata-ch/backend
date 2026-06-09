package ch.agridata.common.exceptions;

import lombok.Getter;

/**
 * Signals that an upstream data provider returned an error response. Carries the provider's HTTP status code and response body so the
 * client can be informed about the underlying failure.
 *
 * @CommentLastReviewed 2026-05-29
 */
@Getter
public class DataProviderException extends RuntimeException {

  private static final String MESSAGE = "Data Provider returned an error";

  private final Integer dataProviderHttpStatus;
  private final String dataProviderMessage;

  public DataProviderException(Integer dataProviderHttpStatus, String dataProviderMessage, Throwable cause) {
    super(MESSAGE, cause);
    this.dataProviderHttpStatus = dataProviderHttpStatus;
    this.dataProviderMessage = dataProviderMessage;
  }

  public DataProviderException(Integer dataProviderHttpStatus, String dataProviderMessage) {
    super(MESSAGE);
    this.dataProviderHttpStatus = dataProviderHttpStatus;
    this.dataProviderMessage = dataProviderMessage;
  }
}
