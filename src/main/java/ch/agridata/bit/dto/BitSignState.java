package ch.agridata.bit.dto;

/**
 * Represents the lifecycle state of a sign process on the BIT evidence Signing API.
 *
 * @CommentLastReviewed 2026-04-09
 */
public enum BitSignState {
  SIGN_PREPARING,
  SIGN_RUNNING,
  SIGN_CANCELED,
  SIGN_FINISHED,
  SIGN_INVALID_STATE,
  SIGN_UNKNOWN
}
