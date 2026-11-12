package ch.agridata.common.security;

import java.util.UUID;
import lombok.Getter;

/**
 * Collects the system actors, i.e. automated processes that act without an authenticated user. Each actor has an {@code actorId}, which
 * identifies it in audit logs, and a technical {@code userId}, under which its work runs via {@link BackgroundTaskRunner} (traceability,
 * {@code modifiedBy}).
 *
 * <p>The technical user of an actor must exist in the {@code users} table, with the {@code actorId} as its given name. The
 * {@code userId} is {@code null} for an actor that only appears in audit logs and never runs as a user.
 *
 * @CommentLastReviewed 2026-11-12
 */

@Getter
public enum SystemActorEnum {
  CONSENT_REQUEST_CLEANUP_JOB("SYSTEM:CONSENT_REQUEST_CLEANUP_JOB", "3899f61d-c517-40da-a4a6-f2b062cc0f20"),
  DATA_REQUEST_TERMINATION_JOB("SYSTEM:DATA_REQUEST_TERMINATION_JOB", "d10bca30-89ba-4e28-b03e-dee5e30ccc50"),
  DOCUMENT_SCAN_RECOVERY_JOB("SYSTEM:DOCUMENT_SCAN_RECOVERY_JOB", "5bb8cb23-6091-4dc1-b2c0-58aac774f832"),
  LEGALLY_PERMITTED_CONSENT("SYSTEM:LEGALLY_PERMITTED_CONSENT", "c39129a1-9035-4497-909f-3ee3488cf022"),
  NOTIFICATION_QUEUE_WORKER("SYSTEM:NOTIFICATION_QUEUE_WORKER", "9fc651e8-def0-4456-a8d4-7d4c9d3dfc04"),
  DATA_REQUEST_STATE_TRANSITION("SYSTEM:DATA_REQUEST_STATE_TRANSITION", null);

  private final String loggingActorId;
  private final UUID userId;

  SystemActorEnum(String loggingActorId, String userId) {
    this.loggingActorId = loggingActorId;
    this.userId = userId == null ? null : UUID.fromString(userId);
  }
}
