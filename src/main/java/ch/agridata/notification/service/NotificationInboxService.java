package ch.agridata.notification.service;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.notification.dto.InboxEntryDto;
import ch.agridata.notification.dto.MarkAsReadRequestDto;
import ch.agridata.notification.mapper.NotificationTargetTypeMapper;
import ch.agridata.notification.persistence.NotificationInboxEntity;
import ch.agridata.notification.persistence.NotificationInboxRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
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
  private final NotificationPlaceholderService placeholderService;
  private final NotificationTargetTypeMapper targetTypeMapper;

  public PageResponseDto<InboxEntryDto> getInboxForUser(UUID userId, ResourceQueryDto query) {
    ResourceQueryDto effectiveQuery = query;
    if (query.sortParams() == null || query.sortParams().isEmpty()) {
      effectiveQuery = ResourceQueryDto.builder()
          .page(query.page())
          .size(query.size())
          .sortParams(List.of("-createdAt"))
          .searchTerm(query.searchTerm())
          .build();
    }
    var pagedEntities = inboxRepository.findPageByUserId(userId, effectiveQuery);
    var items = pagedEntities.items().stream().map(this::toDto).toList();
    return new PageResponseDto<>(
        items,
        pagedEntities.totalItems(),
        pagedEntities.totalPages(),
        pagedEntities.currentPage(),
        pagedEntities.pageSize()
    );
  }

  @Transactional
  public void markReadStatus(UUID userId, MarkAsReadRequestDto request, boolean isRead) {
    int updated = inboxRepository.markReadStatus(userId, request.inboxIds(), isRead);
    if (isRead) {
      log.info("Marked {} inbox entries as read for user {}", updated, userId);
    } else {
      log.info("Marked {} inbox entries as unread for user {}", updated, userId);
    }
  }

  private InboxEntryDto toDto(NotificationInboxEntity entity) {
    var batch = entity.getRecipient().getBatch();
    var resolvedNotificationTexts = placeholderService.resolve(batch);

    return new InboxEntryDto(
        entity.getId(),
        resolvedNotificationTexts.webappTitle(),
        resolvedNotificationTexts.webappText(),
        entity.getUserId(),
        entity.isRead(),
        entity.getCreatedAt(),
        targetTypeMapper.toDtoEnum(batch.getTargetTypeCode()),
        batch.getTargetId()
    );
  }
}
