package ch.agridata.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import ch.agridata.aws.api.EmailApi;
import ch.agridata.common.dto.TranslationDto;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.notification.dto.ResolvedNotificationTextsDto;
import ch.agridata.notification.persistence.NotificationDispatchEntity;
import ch.agridata.notification.persistence.NotificationDispatchRepository;
import ch.agridata.notification.persistence.NotificationDispatchStatusEnum;
import ch.agridata.notification.persistence.NotificationRecipientEntity;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link NotificationSubmitEmailService}.
 * Verifies email dispatch logic, dispatch-entry creation, and error handling without
 * a real database or email infrastructure. Placeholder substitution is the
 * responsibility of {@link NotificationPlaceholderService} and is tested separately.
 *
 * @CommentLastReviewed 2026-05-08
 */
@ExtendWith(MockitoExtension.class)
class NotificationSubmitEmailServiceTest {

  @Mock
  private EmailApi emailApi;

  @Mock
  private NotificationDispatchRepository dispatchRepository;

  @InjectMocks
  private NotificationSubmitEmailService service;

  @Captor
  private ArgumentCaptor<NotificationDispatchEntity> dispatchCaptor;

  // ── fixtures ─────────────────────────────────────────────────────────────

  private static NotificationRecipientEntity recipient() {
    return NotificationRecipientEntity.builder().id(UUID.randomUUID()).email("test@example.com").build();
  }

  private static ResolvedNotificationTextsDto resolvedWith(String subject, String htmlText) {
    return new ResolvedNotificationTextsDto(
        null,
        null,
        TranslationDto.builder().de(subject).build(),
        TranslationDto.builder().de(htmlText).build(),
        null
    );
  }

  private static ResolvedNotificationTextsDto resolvedWithoutSubject() {
    return new ResolvedNotificationTextsDto(null, null, null, TranslationDto.builder().de("body").build(), null);
  }

  private static ResolvedNotificationTextsDto resolvedWithoutEmailText() {
    return new ResolvedNotificationTextsDto(null, null, TranslationDto.builder().de("subject").build(), null, null);
  }

  // ── tests ─────────────────────────────────────────────────────────────────

  @Test
  void givenResolvedWithoutEmailSubject_whenDispatch_thenSkipsEmailAndReturnsTrue() {
    var result = service.dispatch(recipient(), resolvedWithoutSubject());

    assertThat(result).isTrue();
    verifyNoInteractions(emailApi, dispatchRepository);
  }

  @Test
  void givenResolvedWithoutEmailText_whenDispatch_thenSkipsEmailAndReturnsTrue() {
    var result = service.dispatch(recipient(), resolvedWithoutEmailText());

    assertThat(result).isTrue();
    verifyNoInteractions(emailApi, dispatchRepository);
  }

  @Test
  void givenValidResolved_whenDispatch_thenSubmitEmailCalledAndSubmittedDispatchPersisted() {
    var recipient = recipient();
    var resolved = resolvedWith("Welcome", "<p>Hello</p>");

    var result = service.dispatch(recipient, resolved);

    assertThat(result).isTrue();
    verify(emailApi).submitEmail(recipient.getEmail(), "Welcome", "<p>Hello</p>");
    verify(dispatchRepository).persist(dispatchCaptor.capture());
    var dispatch = dispatchCaptor.getValue();
    assertThat(dispatch.getStatusCode()).isEqualTo(NotificationDispatchStatusEnum.SUBMITTED);
    assertThat(dispatch.getError()).isNull();
    assertThat(dispatch.getRecipient()).isEqualTo(recipient);
  }

  @Test
  void givenResolvedDeFields_whenDispatch_thenSubjectAndBodyTakenFromGermanField() {
    var resolved = resolvedWith("Hallo Hans", "<p>Lieber Hans, deine ID ist 42.</p>");

    service.dispatch(recipient(), resolved);

    verify(emailApi).submitEmail("test@example.com", "Hallo Hans", "<p>Lieber Hans, deine ID ist 42.</p>");
  }

  @Test
  void givenEmailApiFails_whenDispatch_thenFailedDispatchPersistedAndReturnsFalse() {
    var recipient = recipient();
    var cause = new RuntimeException("SES error");
    doThrow(new ExternalWebServiceException("dispatch failed", cause)).when(emailApi).submitEmail(any(), any(), any());

    var result = service.dispatch(recipient, resolvedWith("Subject", "Body"));

    assertThat(result).isFalse();
    verify(dispatchRepository).persist(dispatchCaptor.capture());
    var dispatch = dispatchCaptor.getValue();
    assertThat(dispatch.getStatusCode()).isEqualTo(NotificationDispatchStatusEnum.FAILED);
    assertThat(dispatch.getError()).isEqualTo("dispatch failed");
    assertThat(dispatch.getRecipient()).isEqualTo(recipient);
  }

  @Test
  void givenEmailApiFailsWithNullMessage_whenDispatch_thenErrorStoredAsUnknown() {
    doThrow(new ExternalWebServiceException(null, new RuntimeException("cause"))).when(emailApi).submitEmail(any(), any(), any());

    service.dispatch(recipient(), resolvedWith("Subject", "Body"));

    verify(dispatchRepository).persist(dispatchCaptor.capture());
    assertThat(dispatchCaptor.getValue().getError()).isEqualTo("Unknown error");
  }

  @Test
  void givenVeryLongErrorMessage_whenDispatch_thenErrorTruncatedTo1000Chars() {
    String longMessage = "e".repeat(2000);
    doThrow(new ExternalWebServiceException(longMessage, new RuntimeException("cause"))).when(emailApi).submitEmail(any(), any(), any());

    service.dispatch(recipient(), resolvedWith("Subject", "Body"));

    verify(dispatchRepository).persist(dispatchCaptor.capture());
    assertThat(dispatchCaptor.getValue().getError()).hasSize(1000);
  }
}
