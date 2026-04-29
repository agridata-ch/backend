package ch.agridata.agreement.dto;

/**
 * Represents the state of an asynchronous seal attempt for a contract revision.
 *
 * @CommentLastReviewed 2026-04-14
 */
public enum SealAttemptStateEnum {
  NOT_STARTED,
  IN_PROGRESS,
  COMPLETED,
  FAILED
}
