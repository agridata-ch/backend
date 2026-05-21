package ch.agridata.notification.persistence;

/**
 * Enumerates the processing states of a notification batch entry.
 *
 * @CommentLastReviewed 2026-04-22
 */
public enum NotificationBatchStatusEnum {
  PENDING, COMPLETE, FAILED, PARTIALLY_FAILED
}
