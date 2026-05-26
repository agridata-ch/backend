package ch.agridata.notification.persistence;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.persistence.BaseSearchRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Provides persistence operations for notification inbox entries.
 *
 * @CommentLastReviewed 2026-05-06
 */
@ApplicationScoped
public class NotificationInboxRepository extends BaseSearchRepository<NotificationInboxEntity, UUID> {

  public PageResponseDto<NotificationInboxEntity> findPageByUserId(UUID userId, ResourceQueryDto query) {
    return findPage(query, "userId = :userId", Map.of("userId", userId), List.of(), List.of());
  }

  public boolean existsByRecipientId(UUID recipientId) {
    return find("recipient.id", recipientId).count() > 0;
  }

  public int markReadStatus(UUID userId, List<UUID> inboxIds, boolean isRead) {
    return update("isRead = ?1 WHERE userId = ?2 AND id IN ?3", isRead, userId, inboxIds);
  }
}
