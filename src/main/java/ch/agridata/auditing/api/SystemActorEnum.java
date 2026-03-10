package ch.agridata.auditing.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Defines system actors used in audit logging. These actors represent automated system
 * processes rather than authenticated users.
 *
 * @CommentLastReviewed 2026-03-11
 */

@Getter
@AllArgsConstructor
public enum SystemActorEnum {
  CONSENT_REQUEST_CLEANUP_JOB("SYSTEM:CONSENT_REQUEST_CLEANUP_JOB");

  private final String actorId;
}
