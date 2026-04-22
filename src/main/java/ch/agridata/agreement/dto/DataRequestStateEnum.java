package ch.agridata.agreement.dto;

/**
 * Lists the possible states of a data request.
 *
 * @CommentLastReviewed 2025-10-02
 */

public enum DataRequestStateEnum {
  DRAFT,
  IN_REVIEW,
  TO_BE_SIGNED_BY_CONSUMER,
  TO_BE_RELEASED_BY_CONSUMER,
  TO_BE_SIGNED_BY_PROVIDER,
  TO_BE_RELEASED_BY_PROVIDER,
  TO_BE_ACTIVATED,
  ACTIVE,
}
