package ch.agridata.common.exceptions;

/**
 * Indicates that an OTP verification attempt was rejected because the submitted code does not match
 * the stored hash, the challenge was already consumed, or the challenge identity does not match.
 * The plaintext OTP code is intentionally never carried by this exception.
 *
 * @CommentLastReviewed 2026-05-08
 */
public class OtpInvalidException extends RuntimeException {

  public OtpInvalidException(String message) {
    super(message);
  }
}
