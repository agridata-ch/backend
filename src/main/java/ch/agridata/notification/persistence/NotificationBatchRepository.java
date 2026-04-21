package ch.agridata.notification.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

/**
 * Provides persistence operations for notification batch entries.
 *
 * @CommentLastReviewed 2026-04-22
 */
@ApplicationScoped
public class NotificationBatchRepository implements PanacheRepositoryBase<NotificationBatchEntity, UUID> {

  /**
   * Finds all PENDING batch entries and locks them for update, skipping already-locked rows.
   * This prevents concurrent processing across multiple application instances.
   */
  @SuppressWarnings("unchecked")
  public List<NotificationBatchEntity> findPendingWithLock() {
    return getEntityManager()
        .createNativeQuery(
            "SELECT * FROM notification_batch WHERE status_code = 'PENDING' AND archived = false FOR UPDATE SKIP LOCKED",
            NotificationBatchEntity.class)
        .getResultList();
  }
}
