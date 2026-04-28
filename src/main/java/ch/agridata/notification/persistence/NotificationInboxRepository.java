package ch.agridata.notification.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

/**
 * Provides persistence operations for notification inbox entries.
 *
 * @CommentLastReviewed 2026-04-22
 */
@ApplicationScoped
public class NotificationInboxRepository implements PanacheRepositoryBase<NotificationInboxEntity, UUID> {

  public List<NotificationInboxEntity> findByUserId(UUID userId) {
    return list("userId = ?1 ORDER BY createdAt DESC", userId);
  }

  public boolean existsByRecipientId(UUID recipientId) {
    return find("recipient.id", recipientId).count() > 0;
  }

  public int markAsRead(UUID userId, List<UUID> inboxIds) {
    return update("isRead = true WHERE userId = ?1 AND id IN ?2", userId, inboxIds);
  }
}
