package ch.agridata.auditing.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Defines system actors used in audit logging. These actors represent automated system
 * processes rather than authenticated users.
 *
 * @CommentLastReviewed 2026-06-01
 */

@Getter
@AllArgsConstructor
public enum SystemActorEnum {
  CONSENT_REQUEST_CLEANUP_JOB("SYSTEM:CONSENT_REQUEST_CLEANUP_JOB"),
  DATA_REQUEST_STATE_TRANSITION("SYSTEM:DATA_REQUEST_STATE_TRANSITION");

  private final String actorId;
}
