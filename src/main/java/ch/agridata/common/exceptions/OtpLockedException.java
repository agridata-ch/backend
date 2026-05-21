package ch.agridata.common.exceptions;

/**
 * Indicates that an OTP challenge is locked because the maximum number of failed attempts has been
 * reached.
 *
 * @CommentLastReviewed 2026-05-08
 */
public class OtpLockedException extends RuntimeException {

  public OtpLockedException(String message) {
    super(message);
  }
}
