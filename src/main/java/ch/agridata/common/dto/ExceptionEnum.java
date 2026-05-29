package ch.agridata.common.dto;

/**
 * Lists generic exception categories across the system. It standardizes error reporting for internal and client-facing responses.
 *
 * @CommentLastReviewed 2026-05-08
 */
public enum ExceptionEnum {
  GENERIC,
  UID_MISSING,
  EXTERNAL_SERVICE_ERROR,
  CONSENT_NOT_GRANTED,
  OTP_INVALID,
  OTP_LOCKED,
  OTP_EXPIRED,
  OTP_RESEND_COOLDOWN,
  DATA_PROVIDER_ERROR,
  MAINTENANCE // Used by the AWS load balancer to indicate that the service is temporarily unavailable due to maintenance
}
