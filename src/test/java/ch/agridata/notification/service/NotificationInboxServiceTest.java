package ch.agridata.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.notification.dto.InboxEntryDto;
import ch.agridata.notification.mapper.NotificationInboxMapper;
import ch.agridata.notification.persistence.NotificationBatchEntity;
import ch.agridata.notification.persistence.NotificationInboxEntity;
import ch.agridata.notification.persistence.NotificationInboxRepository;
import ch.agridata.notification.persistence.NotificationRecipientEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link NotificationInboxService}.
 *
 * @CommentLastReviewed 2026-05-06
 */
@ExtendWith(MockitoExtension.class)
class NotificationInboxServiceTest {

  @InjectMocks
  private NotificationInboxService service;

  @Mock
  private NotificationInboxRepository inboxRepository;

  @Mock
  private NotificationInboxMapper inboxMapper;

  @Test
  void givenUserHasInboxEntries_whenGetInbox_thenReturnsDtos() {
    var userId = UUID.randomUUID();
    var recipientId = UUID.randomUUID();
    var query = ResourceQueryDto.builder().page(0).size(20).build();
    var batch = NotificationBatchEntity.builder().id(UUID.randomUUID()).build();
    var recipient = NotificationRecipientEntity.builder().id(recipientId).batch(batch).userId(userId).build();
    var entity = NotificationInboxEntity.builder().id(UUID.randomUUID()).userId(userId).isRead(false).recipient(recipient).build();
    var dto = new InboxEntryDto(entity.getId(), recipientId, userId, false, LocalDateTime.now());
    var pagedEntities = new PageResponseDto<>(List.of(entity), 1L, 1, 0, 20);
    var pagedDtos = new PageResponseDto<>(List.of(dto), 1L, 1, 0, 20);

    when(inboxRepository.findPageByUserId(userId, query)).thenReturn(pagedEntities);
    when(inboxMapper.toPagedDto(pagedEntities)).thenReturn(pagedDtos);

    var result = service.getInboxForUser(userId, query);

    assertThat(result.items()).hasSize(1);
    assertThat(result.items().getFirst().userId()).isEqualTo(userId);
    assertThat(result.items().getFirst().isRead()).isFalse();
    assertThat(result.items().getFirst().recipientId()).isEqualTo(recipientId);
  }

  @Test
  void givenUserHasNoInboxEntries_whenGetInbox_thenReturnsEmptyPage() {
    var userId = UUID.randomUUID();
    var query = ResourceQueryDto.builder().page(0).size(20).build();
    var pagedEntities = new PageResponseDto<NotificationInboxEntity>(List.of(), 0L, 0, 0, 20);
    var pagedDtos = new PageResponseDto<InboxEntryDto>(List.of(), 0L, 0, 0, 20);

    when(inboxRepository.findPageByUserId(userId, query)).thenReturn(pagedEntities);
    when(inboxMapper.toPagedDto(pagedEntities)).thenReturn(pagedDtos);

    var result = service.getInboxForUser(userId, query);

    assertThat(result.items()).isEmpty();
    assertThat(result.totalItems()).isZero();
  }
}
