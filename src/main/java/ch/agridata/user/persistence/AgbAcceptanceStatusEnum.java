package ch.agridata.user.persistence;

/**
 * Enumerates the AGB acceptance states of a user as derived by the {@code status} filter in {@link UserRepository#findAgbAcceptancePage}.
 *
 * @CommentLastReviewed 2026-10-21
 */
public enum AgbAcceptanceStatusEnum {

  ACCEPTED,
  TRANSITION_PERIOD,
  PENDING
}
