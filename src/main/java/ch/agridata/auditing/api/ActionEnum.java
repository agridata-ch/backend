package ch.agridata.auditing.api;

/**
 * Defines the set of supported actions that can be recorded. It ensures standardized naming for audit events.
 *
 * @CommentLastReviewed 2025-08-25
 */

public enum ActionEnum {
  CONSENT_REQUEST_REOPENED,
  CONSENT_REQUEST_GRANTED,
  CONSENT_REQUEST_DECLINED
}
