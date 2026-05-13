package ch.agridata.notification.service;

/**
 * CDI event fired after a notification batch has been queued. Observed in
 * {@code NotificationQueueWorkerJob} to trigger immediate processing instead of
 * waiting for the scheduled cron run.
 *
 * @CommentLastReviewed 2026-05-13
 */
public record NotificationBatchQueuedEvent() {
}
