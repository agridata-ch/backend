package ch.agridata.notification.persistence;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

/**
 * Provides persistence operations for notification inbox entries.
 *
 * @CommentLastReviewed 2026-05-06
 */
@ApplicationScoped
public class NotificationInboxRepository implements PanacheRepositoryBase<NotificationInboxEntity, UUID> {

  public List<NotificationInboxEntity> findByUserId(UUID userId) {
    return list("userId = ?1 ORDER BY createdAt DESC", userId);
  }

  public PageResponseDto<NotificationInboxEntity> findPageByUserId(UUID userId, ResourceQueryDto query) {
    var paged = find("userId = ?1", Sort.descending("createdAt"), userId)
        .page(query.page(), query.size());
    return new PageResponseDto<>(paged.list(), paged.count(), paged.pageCount(), query.page(), query.size());
  }

  public boolean existsByRecipientId(UUID recipientId) {
    return find("recipient.id", recipientId).count() > 0;
  }

  public int markAsRead(UUID userId, List<UUID> inboxIds) {
    return update("isRead = true WHERE userId = ?1 AND id IN ?2", userId, inboxIds);
  }
}
