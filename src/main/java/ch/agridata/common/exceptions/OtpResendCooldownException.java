package ch.agridata.common.exceptions;

import lombok.Getter;

/**
 * Indicates that a new OTP challenge cannot be created yet because the resend cooldown for the same
 * user, contract revision, and signature slot has not elapsed. Carries the remaining cooldown so the
 * client can render an actionable countdown.
 *
 * @CommentLastReviewed 2026-05-08
 */
@Getter
public class OtpResendCooldownException extends RuntimeException {

  private final long retryAfterSeconds;

  public OtpResendCooldownException(long retryAfterSeconds) {
    super("OTP was sent too recently. Please wait " + retryAfterSeconds + " seconds before requesting another code.");
    this.retryAfterSeconds = retryAfterSeconds;
  }
}
