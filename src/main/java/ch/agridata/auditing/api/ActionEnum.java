package ch.agridata.auditing.api;

/**
 * Defines the set of supported actions that can be recorded. It ensures standardized naming for audit events.
 *
 * @CommentLastReviewed 2026-01-22
 */

public enum ActionEnum {
  CONSENT_REQUEST_REOPENED,
  CONSENT_REQUEST_GRANTED,
  CONSENT_REQUEST_DECLINED,
  DATA_REQUEST_SUBMITTED,
  DATA_REQUEST_REJECTED,
  DATA_REQUEST_APPROVED,
  DATA_REQUEST_ACTIVATED,
  DATA_REQUEST_WITHDRAWN
}
