package ch.agridata.notification.service;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.notification.dto.InboxEntryDto;
import ch.agridata.notification.dto.MarkAsReadRequestDto;
import ch.agridata.notification.mapper.NotificationInboxMapper;
import ch.agridata.notification.persistence.NotificationInboxRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reads inbox entries for a given user.
 *
 * @CommentLastReviewed 2026-05-06
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class NotificationInboxService {

  private final NotificationInboxRepository inboxRepository;
  private final NotificationInboxMapper inboxMapper;

  public PageResponseDto<InboxEntryDto> getInboxForUser(UUID userId, ResourceQueryDto query) {
    var pagedEntities = inboxRepository.findPageByUserId(userId, query);
    return inboxMapper.toPagedDto(pagedEntities);
  }

  @Transactional
  public void markAsRead(UUID userId, MarkAsReadRequestDto request) {
    int updated = inboxRepository.markAsRead(userId, request.inboxIds());
    log.info("Marked {} inbox entries as read for user {}", updated, userId);
  }
}
