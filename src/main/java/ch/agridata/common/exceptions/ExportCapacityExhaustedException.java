package ch.agridata.common.exceptions;

import lombok.Getter;

/**
 * Indicates that an export was rejected because the maximum number of concurrent exports is reached. Carries the number of
 * seconds after which the client may retry.
 *
 * @CommentLastReviewed 2026-10-12
 */
@Getter
public class ExportCapacityExhaustedException extends RuntimeException {

  private final int retryAfterSeconds;

  public ExportCapacityExhaustedException(int retryAfterSeconds) {
    super("Maximum number of concurrent exports is reached. Please retry in " + retryAfterSeconds + " seconds.");
    this.retryAfterSeconds = retryAfterSeconds;
  }
}
