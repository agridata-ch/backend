package ch.agridata.auditing.api;

import java.util.UUID;

/**
 * Defines the interface for recording user actions in the system. It ensures a consistent contract for auditing across services.
 *
 * @CommentLastReviewed 2025-08-25
 */

public interface AuditingApi {

  void logUserAction(ActionEnum actionCode,
                     EntityTypeEnum entityTypeCode,
                     UUID entityId);

}
