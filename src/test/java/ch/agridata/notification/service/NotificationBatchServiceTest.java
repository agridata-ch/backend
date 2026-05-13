package ch.agridata.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.common.persistence.TranslationPersistenceDto;
import ch.agridata.notification.dto.EventTypeCodeEnum;
import ch.agridata.notification.dto.RecipientRequestDto;
import ch.agridata.notification.persistence.NotificationBatchEntity;
import ch.agridata.notification.persistence.NotificationBatchRepository;
import ch.agridata.notification.persistence.NotificationBatchStatusEnum;
import ch.agridata.notification.persistence.NotificationRecipientEntity;
import ch.agridata.notification.persistence.NotificationRecipientRepository;
import ch.agridata.notification.persistence.NotificationTemplateEntity;
import ch.agridata.notification.persistence.NotificationTemplateRepository;
import jakarta.enterprise.event.Event;
import jakarta.ws.rs.NotFoundException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link NotificationBatchService}.
 *
 * @CommentLastReviewed 2026-05-18
 */
@ExtendWith(MockitoExtension.class)
class NotificationBatchServiceTest {

  @InjectMocks
  private NotificationBatchService service;

  @Mock
  private NotificationTemplateRepository templateRepository;

  @Mock
  private NotificationBatchRepository batchRepository;

  @Mock
  private NotificationRecipientRepository recipientRepository;

  @Mock
  private NotificationPlaceholderService placeholderService;

  @Mock
  private Event<NotificationBatchQueuedEvent> batchQueuedEvent;

  @Test
  void givenUserAndEmailRecipients_whenQueueNotification_thenPersistsBatchAndRecipients() {
    var template = NotificationTemplateEntity.builder()
        .id(UUID.randomUUID())
        .eventTypeCode(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW.name())
        .build();
    var recipients = List.of(new RecipientRequestDto(UUID.randomUUID(), null), new RecipientRequestDto(null, "user@example.com"));
    var placeholders = Map.of("requestTitle", "Test");

    when(templateRepository.findLatestByEventTypeCode(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW.name())).thenReturn(Optional.of(
        template));

    service.queueNotification(recipients, EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW, placeholders);

    var batchCaptor = ArgumentCaptor.forClass(NotificationBatchEntity.class);
    verify(batchRepository).persist(batchCaptor.capture());
    var batch = batchCaptor.getValue();
    assertThat(batch.getStatusCode()).isEqualTo(NotificationBatchStatusEnum.PENDING);
    assertThat(batch.getPlaceholders()).isEqualTo(placeholders);
    assertThat(batch.getTemplate()).isEqualTo(template);

    var recipientCaptor = ArgumentCaptor.forClass(NotificationRecipientEntity.class);
    verify(recipientRepository, times(2)).persist(recipientCaptor.capture());
    var persistedRecipients = recipientCaptor.getAllValues();
    assertThat(persistedRecipients).extracting(NotificationRecipientEntity::getEmail).containsExactlyInAnyOrder(null, "user@example.com");

    verify(batchQueuedEvent).fire(any(NotificationBatchQueuedEvent.class));
  }

  @Test
  void givenValidRequest_whenQueueNotification_thenUsesLatestTemplate() {
    var template = NotificationTemplateEntity.builder()
        .id(UUID.randomUUID())
        .eventTypeCode(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW.name())
        .templateVersion(2)
        .build();
    var recipients = List.of(new RecipientRequestDto(UUID.randomUUID(), null));

    when(templateRepository.findLatestByEventTypeCode(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW.name())).thenReturn(Optional.of(
        template));

    service.queueNotification(recipients, EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW, Map.of());

    var captor = ArgumentCaptor.forClass(NotificationBatchEntity.class);
    verify(batchRepository).persist(captor.capture());
    assertThat(captor.getValue().getTemplate().getTemplateVersion()).isEqualTo(2);
  }

  @Test
  void givenNoTemplate_whenQueueNotification_thenThrowsNotFoundException() {
    when(templateRepository.findLatestByEventTypeCode(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW.name())).thenReturn(Optional.empty());
    var recipientList = List.of(new RecipientRequestDto(UUID.randomUUID(), null));
    Map<String, String> emptyMap = Collections.emptyMap();

    assertThatThrownBy(() -> service.queueNotification(
        recipientList,
        EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW,
        emptyMap
    )).isInstanceOf(NotFoundException.class);

    verify(batchRepository, never()).persist(any(NotificationBatchEntity.class));
    verify(recipientRepository, never()).persist(any(NotificationRecipientEntity.class));
    verify(batchQueuedEvent, never()).fire(any(NotificationBatchQueuedEvent.class));
  }

  @Test
  void givenMissingRequiredPlaceholder_whenQueueNotification_thenThrowsIllegalArgumentException() {
    var template = NotificationTemplateEntity.builder()
        .id(UUID.randomUUID())
        .eventTypeCode(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW.name())
        .build();
    when(templateRepository.findLatestByEventTypeCode(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW.name()))
        .thenReturn(Optional.of(template));
    when(placeholderService.extractRequiredPlaceholders(template))
        .thenReturn(new LinkedHashSet<>(List.of("dataRequestTitleDe", "dataConsumer")));

    assertThatThrownBy(() -> service.queueNotification(
        List.of(new RecipientRequestDto(UUID.randomUUID(), null)),
        EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW,
        Map.of("dataRequestTitleDe", "Mein Antrag")  // dataConsumer missing
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("dataConsumer");

    verify(batchRepository, never()).persist(any(NotificationBatchEntity.class));
  }

  @Test
  void givenAllRequiredPlaceholdersPresent_whenQueueNotification_thenPersistsBatch() {
    var template = NotificationTemplateEntity.builder()
        .id(UUID.randomUUID())
        .eventTypeCode(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW.name())
        .build();
    when(templateRepository.findLatestByEventTypeCode(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW.name()))
        .thenReturn(Optional.of(template));
    when(placeholderService.extractRequiredPlaceholders(template))
        .thenReturn(new LinkedHashSet<>(List.of("dataRequestTitleDe", "dataConsumer")));

    service.queueNotification(
        List.of(new RecipientRequestDto(UUID.randomUUID(), null)),
        EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW,
        Map.of("dataRequestTitleDe", "Mein Antrag", "dataConsumer", "Bio Suisse")
    );

    verify(batchRepository).persist(any(NotificationBatchEntity.class));
  }

  @Test
  void givenTemplateWithoutPlaceholders_whenQueueNotification_thenSkipsValidationAndPersistsBatch() {
    var template = NotificationTemplateEntity.builder()
        .id(UUID.randomUUID())
        .eventTypeCode(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW.name())
        .emailText(new TranslationPersistenceDto("Statischer Text", "Texte statique", "Testo statico"))
        .build();
    when(templateRepository.findLatestByEventTypeCode(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW.name()))
        .thenReturn(Optional.of(template));
    when(placeholderService.extractRequiredPlaceholders(template)).thenReturn(Set.of());

    service.queueNotification(
        List.of(new RecipientRequestDto(UUID.randomUUID(), null)),
        EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW,
        Map.of()
    );

    verify(batchRepository).persist(any(NotificationBatchEntity.class));
  }
}
