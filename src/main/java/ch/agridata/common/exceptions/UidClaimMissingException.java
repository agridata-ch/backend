package ch.agridata.common.exceptions;

/**
 * Indicates that the current caller's security identity (JWT) has no {@code uid} claim. Signals an
 * authorization-shaped failure for self-service, JWT-only UID resolution, as opposed to {@link UidMissingException}
 * which signals that an external UID synchronization may still be pending.
 *
 * @CommentLastReviewed 2026-09-16
 */
public class UidClaimMissingException extends RuntimeException {

  public UidClaimMissingException(String message) {
    super(message);
  }

}
