package ch.agridata.common.exceptions;

import lombok.Getter;

/**
 * This exception is deprecated.
 *
 * <p>Signals that a data transfer process has failed. It encapsulates error information for consistent reporting.
 *
 * @CommentLastReviewed 2026-05-29
 * @deprecated Replaced by {@link ch.agridata.common.exceptions.DataProviderException}
 */
@Deprecated(since = "1.10.0")
@Getter
public class DataTransferFailedException extends RuntimeException {

  private final int status;

  public DataTransferFailedException(int status, String message, Throwable cause) {
    super(message, cause);
    this.status = status;
  }

  public DataTransferFailedException(int status, String message) {
    super(message);
    this.status = status;
  }
}
