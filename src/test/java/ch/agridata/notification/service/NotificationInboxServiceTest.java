package ch.agridata.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.dto.TranslationDto;
import ch.agridata.notification.dto.ResolvedNotificationTextsDto;
import ch.agridata.notification.mapper.NotificationTargetTypeMapperImpl;
import ch.agridata.notification.persistence.NotificationBatchEntity;
import ch.agridata.notification.persistence.NotificationInboxEntity;
import ch.agridata.notification.persistence.NotificationInboxRepository;
import ch.agridata.notification.persistence.NotificationRecipientEntity;
import ch.agridata.notification.persistence.TargetTypeCodeEnum;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link NotificationInboxService}.
 *
 * @CommentLastReviewed 2026-05-08
 */
@ExtendWith(MockitoExtension.class)
class NotificationInboxServiceTest {

  @InjectMocks
  private NotificationInboxService service;

  @Mock
  private NotificationInboxRepository inboxRepository;

  @Mock
  private NotificationPlaceholderService placeholderService;

  @Spy
  private NotificationTargetTypeMapperImpl targetTypeMapper;

  @Test
  void givenUserHasInboxEntries_whenGetInbox_thenReturnsDtosWithSubstitutedPlaceholders() {
    var userId = UUID.randomUUID();
    var query = ResourceQueryDto.builder().page(0).size(20).build();
    var targetTypeCode = TargetTypeCodeEnum.DATA_REQUEST;

    var notificationBatchEntity = NotificationBatchEntity.builder()
        .id(UUID.randomUUID())
        .targetTypeCode(targetTypeCode)
        .build();
    var notificationRecipientEntity = NotificationRecipientEntity.builder()
        .id(UUID.randomUUID())
        .batch(notificationBatchEntity)
        .build();
    var notificationInboxEntity = NotificationInboxEntity.builder()
        .id(UUID.randomUUID())
        .userId(userId)
        .isRead(false)
        .recipient(notificationRecipientEntity)
        .build();
    notificationInboxEntity.setCreatedAt(LocalDateTime.now());
    var pagedEntities = new PageResponseDto<>(List.of(notificationInboxEntity), 1L, 1, 0, 20);

    var titleDto = TranslationDto.builder().de("Titel Welt").fr("Titre Welt").it("Titolo Welt").build();
    var textDto = TranslationDto.builder().de("Text Welt").fr("Texte Welt").it("Testo Welt").build();
    var resolved = new ResolvedNotificationTextsDto(titleDto, textDto, null, null, null);

    when(inboxRepository.findPageByUserId(userId, query)).thenReturn(pagedEntities);
    when(placeholderService.resolve(notificationBatchEntity)).thenReturn(resolved);

    var result = service.getInboxForUser(userId, query);

    assertThat(result.items()).hasSize(1);
    var item = result.items().getFirst();
    assertThat(item.userId()).isEqualTo(userId);
    assertThat(item.isRead()).isFalse();
    assertThat(item.title()).isEqualTo(titleDto);
    assertThat(item.text()).isEqualTo(textDto);
    assertThat(item.targetType().name()).isEqualTo(targetTypeCode.name());
  }

  @Test
  void givenUserHasNoInboxEntries_whenGetInbox_thenReturnsEmptyPage() {
    var userId = UUID.randomUUID();
    var query = ResourceQueryDto.builder().page(0).size(20).build();
    var pagedEntities = new PageResponseDto<NotificationInboxEntity>(List.of(), 0L, 0, 0, 20);

    when(inboxRepository.findPageByUserId(userId, query)).thenReturn(pagedEntities);

    var result = service.getInboxForUser(userId, query);

    assertThat(result.items()).isEmpty();
    assertThat(result.totalItems()).isZero();
  }
}