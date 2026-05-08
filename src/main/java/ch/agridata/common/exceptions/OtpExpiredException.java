package ch.agridata.common.exceptions;

/**
 * Indicates that an OTP challenge has expired and can no longer be verified.
 *
 * @CommentLastReviewed 2026-05-08
 */
public class OtpExpiredException extends RuntimeException {

  public OtpExpiredException(String message) {
    super(message);
  }
}
