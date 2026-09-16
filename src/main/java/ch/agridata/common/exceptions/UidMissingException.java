package ch.agridata.common.exceptions;

/**
 * Indicates that no UID is currently associated with the account because an external UID synchronization
 * may still be pending. Signals a transient, retryable condition, as opposed to {@link UidClaimMissingException}
 * which signals that the caller's JWT carries no {@code uid} claim at all.
 *
 * @CommentLastReviewed 2026-09-16
 */
public class UidMissingException extends RuntimeException {

  public UidMissingException(String message) {
    super(message);
  }

}
