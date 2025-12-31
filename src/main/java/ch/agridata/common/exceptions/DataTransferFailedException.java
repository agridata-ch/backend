package ch.agridata.common.exceptions;

import lombok.Getter;

/**
 * Signals that a data transfer process has failed. It encapsulates error information for consistent reporting.
 *
 * @CommentLastReviewed 2025-08-25
 */
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
