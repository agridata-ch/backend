package ch.agridata.notification.mapper;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.notification.dto.InboxEntryDto;
import ch.agridata.notification.persistence.NotificationInboxEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps notification inbox entities to DTOs.
 *
 * @CommentLastReviewed 2026-05-06
 */
@Mapper(componentModel = "jakarta")
public interface NotificationInboxMapper {

  @Mapping(target = "recipientId", source = "recipient.id")
  @Mapping(target = "isRead", source = "read")
  InboxEntryDto toDto(NotificationInboxEntity entity);

  List<InboxEntryDto> toDtoList(List<NotificationInboxEntity> entities);

  PageResponseDto<InboxEntryDto> toPagedDto(PageResponseDto<NotificationInboxEntity> pagedEntities);
}
