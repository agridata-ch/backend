package ch.agridata.notification.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

/**
 * Provides persistence operations for notification recipient entries.
 *
 * @CommentLastReviewed 2026-04-22
 */
@ApplicationScoped
public class NotificationRecipientRepository implements PanacheRepositoryBase<NotificationRecipientEntity, UUID> {

  /**
   * Finds all recipients for a given batch ID.
   */
  public List<NotificationRecipientEntity> findByBatchId(UUID batchId) {
    return list("batch.id = ?1", batchId);
  }
}
