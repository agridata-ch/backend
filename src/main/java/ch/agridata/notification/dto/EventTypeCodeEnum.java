package ch.agridata.notification.dto;

/**
 * Enumerates event types that trigger notifications.
 *
 * @CommentLastReviewed 2026-05-06
 */
public enum EventTypeCodeEnum {
  DATA_REQUEST_READY_FOR_REVIEW,
  DATA_REQUEST_CHANGES_NEEDED,
  DATA_REQUEST_APPROVED,
  DATA_REQUEST_READY_FOR_PROVIDER_SIGNING,
  DATA_REQUEST_READY_FOR_ACTIVATION,
  DATA_REQUEST_ACTIVATED,
}
