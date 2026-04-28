package ch.agridata.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.common.persistence.TranslationPersistenceDto;
import ch.agridata.notification.persistence.NotificationDispatchRepository;
import ch.agridata.notification.persistence.NotificationInboxEntity;
import ch.agridata.notification.persistence.NotificationInboxRepository;
import ch.agridata.notification.persistence.NotificationRecipientEntity;
import ch.agridata.notification.persistence.NotificationRecipientRepository;
import ch.agridata.notification.persistence.NotificationTemplateEntity;
import ch.agridata.notification.service.NotificationProcessRecipientService.RecipientProcessingResult;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link NotificationProcessRecipientService}.
 *
 * @CommentLastReviewed 2026-04-28
 */
@ExtendWith(MockitoExtension.class)
class NotificationProcessRecipientServiceTest {

  @InjectMocks
  private NotificationProcessRecipientService service;

  @Mock
  private NotificationRecipientRepository recipientRepository;

  @Mock
  private NotificationInboxRepository inboxRepository;

  @Mock
  private NotificationDispatchRepository dispatchRepository;

  @Mock
  private NotificationSubmitEmailService emailDispatchService;

  @Captor
  private ArgumentCaptor<NotificationInboxEntity> inboxCaptor;

  private static final NotificationTemplateEntity TEMPLATE = NotificationTemplateEntity.builder()
      .id(UUID.randomUUID())
      .eventTypeCode("TEST_EVENT")
      .emailSubject(new TranslationPersistenceDto("Betreff", null, null))
      .emailText(new TranslationPersistenceDto("<p>Text</p>", null, null))
      .build();

  @Test
  void givenUserRecipient_whenInboxNotYetCreated_thenCreatesInbox() {
    var userId = UUID.randomUUID();
    var recipient = NotificationRecipientEntity.builder().id(UUID.randomUUID()).userId(userId).build();

    when(recipientRepository.findByIdOptional(recipient.getId())).thenReturn(Optional.of(recipient));
    when(inboxRepository.existsByRecipientId(recipient.getId())).thenReturn(false);

    RecipientProcessingResult result = service.processRecipient(recipient.getId(), TEMPLATE, null);

    verify(inboxRepository).existsByRecipientId(recipient.getId());
    verify(inboxRepository).persist((NotificationInboxEntity) inboxCaptor.capture());
    assertThat(inboxCaptor.getValue().getUserId()).isEqualTo(userId);
    assertThat(inboxCaptor.getValue().isRead()).isFalse();
    assertThat(result.inboxCreated()).isTrue();
  }

  @Test
  void givenUserRecipient_whenInboxAlreadyExists_thenSkipsInboxCreation() {
    var recipient = NotificationRecipientEntity.builder().id(UUID.randomUUID()).userId(UUID.randomUUID()).build();

    when(recipientRepository.findByIdOptional(recipient.getId())).thenReturn(Optional.of(recipient));
    when(inboxRepository.existsByRecipientId(recipient.getId())).thenReturn(true);

    RecipientProcessingResult result = service.processRecipient(recipient.getId(), TEMPLATE, null);

    verify(inboxRepository).existsByRecipientId(recipient.getId());
    verify(inboxRepository, never()).persist((NotificationInboxEntity) any());
    assertThat(result.inboxCreated()).isFalse();
  }

  @Test
  void givenEmailRecipient_whenNotYetDispatched_thenDispatchesEmail() {
    var recipient = NotificationRecipientEntity.builder().id(UUID.randomUUID()).email("user@example.com").build();

    when(recipientRepository.findByIdOptional(recipient.getId())).thenReturn(Optional.of(recipient));
    when(dispatchRepository.existsSentByRecipientId(recipient.getId())).thenReturn(false);
    when(emailDispatchService.dispatch(recipient, TEMPLATE, null)).thenReturn(true);

    RecipientProcessingResult result = service.processRecipient(recipient.getId(), TEMPLATE, null);

    verify(dispatchRepository).existsSentByRecipientId(recipient.getId());
    verify(emailDispatchService).dispatch(recipient, TEMPLATE, null);
    assertThat(result.emailSubmitted()).isTrue();
    assertThat(result.emailSubmissionFailed()).isFalse();
  }

  @Test
  void givenEmailRecipient_whenAlreadyDispatched_thenSkipsEmail() {
    var recipient = NotificationRecipientEntity.builder().id(UUID.randomUUID()).email("user@example.com").build();

    when(recipientRepository.findByIdOptional(recipient.getId())).thenReturn(Optional.of(recipient));
    when(dispatchRepository.existsSentByRecipientId(recipient.getId())).thenReturn(true);

    RecipientProcessingResult result = service.processRecipient(recipient.getId(), TEMPLATE, null);

    verify(dispatchRepository).existsSentByRecipientId(recipient.getId());
    verify(emailDispatchService, never()).dispatch(any(), any(), any());
    assertThat(result.emailSubmitted()).isFalse();
    assertThat(result.emailSubmissionFailed()).isFalse();
  }

  @Test
  void givenEmailRecipient_whenDispatchFails_thenReturnsEmailFailed() {
    var recipient = NotificationRecipientEntity.builder().id(UUID.randomUUID()).email("user@example.com").build();

    when(recipientRepository.findByIdOptional(recipient.getId())).thenReturn(Optional.of(recipient));
    when(dispatchRepository.existsSentByRecipientId(recipient.getId())).thenReturn(false);
    when(emailDispatchService.dispatch(recipient, TEMPLATE, null)).thenReturn(false);

    RecipientProcessingResult result = service.processRecipient(recipient.getId(), TEMPLATE, null);

    assertThat(result.emailSubmissionFailed()).isTrue();
    assertThat(result.emailSubmitted()).isFalse();
  }

  @Test
  void givenMixedRecipient_whenNeitherExists_thenCreatesInboxAndDispatchesEmail() {
    var userId = UUID.randomUUID();
    var recipient = NotificationRecipientEntity.builder().id(UUID.randomUUID()).userId(userId).email("user@example.com").build();

    when(recipientRepository.findByIdOptional(recipient.getId())).thenReturn(Optional.of(recipient));
    when(inboxRepository.existsByRecipientId(recipient.getId())).thenReturn(false);
    when(dispatchRepository.existsSentByRecipientId(recipient.getId())).thenReturn(false);
    when(emailDispatchService.dispatch(recipient, TEMPLATE, null)).thenReturn(true);

    RecipientProcessingResult result = service.processRecipient(recipient.getId(), TEMPLATE, null);

    verify(inboxRepository).existsByRecipientId(recipient.getId());
    verify(dispatchRepository).existsSentByRecipientId(recipient.getId());
    verify(inboxRepository).persist((NotificationInboxEntity) any());
    assertThat(result.inboxCreated()).isTrue();
    assertThat(result.emailSubmitted()).isTrue();
  }
}
