package ch.agridata.notification.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

/**
 * Provides persistence operations for notification dispatch records.
 *
 * @CommentLastReviewed 2026-04-28
 */
@ApplicationScoped
public class NotificationDispatchRepository implements PanacheRepositoryBase<NotificationDispatchEntity, UUID> {

  /**
   * Returns whether a SENT dispatch record already exists for the given recipient.
   * Used for idempotency checks when retrying a batch after a partial failure.
   */
  public boolean existsSentByRecipientId(UUID recipientId) {
    return find("recipient.id = ?1 and statusCode = ?2", recipientId, NotificationDispatchStatusEnum.SUBMITTED).count() > 0;
  }
}
