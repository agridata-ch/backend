package ch.agridata.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import ch.agridata.aws.api.EmailApi;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.common.persistence.TranslationPersistenceDto;
import ch.agridata.notification.persistence.NotificationDispatchEntity;
import ch.agridata.notification.persistence.NotificationDispatchRepository;
import ch.agridata.notification.persistence.NotificationDispatchStatusEnum;
import ch.agridata.notification.persistence.NotificationRecipientEntity;
import ch.agridata.notification.persistence.NotificationTemplateEntity;
import java.util.Map;
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
 * Verifies email dispatch logic, placeholder substitution, dispatch-entry creation,
 * and error handling without a real database or email infrastructure.
 *
 * @CommentLastReviewed 2026-05-05
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

  private static NotificationTemplateEntity templateWith(String subject, String htmlText) {
    return NotificationTemplateEntity.builder()
        .id(UUID.randomUUID())
        .emailSubject(new TranslationPersistenceDto(subject, null, null))
        .emailText(new TranslationPersistenceDto(htmlText, null, null))
        .build();
  }

  private static NotificationTemplateEntity templateWithoutSubject() {
    return NotificationTemplateEntity.builder().id(UUID.randomUUID()).emailText(new TranslationPersistenceDto("body", null, null)).build();
  }

  private static NotificationTemplateEntity templateWithoutEmailText() {
    return NotificationTemplateEntity.builder()
        .id(UUID.randomUUID())
        .emailSubject(new TranslationPersistenceDto("subject", null, null))
        .build();
  }

  // ── tests ─────────────────────────────────────────────────────────────────

  @Test
  void givenTemplateWithoutEmailSubject_whenDispatch_thenSkipsEmailAndReturnsTrue() {
    var result = service.dispatch(recipient(), templateWithoutSubject(), Map.of());

    assertThat(result).isTrue();
    verifyNoInteractions(emailApi, dispatchRepository);
  }

  @Test
  void givenTemplateWithoutEmailText_whenDispatch_thenSkipsEmailAndReturnsTrue() {
    var result = service.dispatch(recipient(), templateWithoutEmailText(), Map.of());

    assertThat(result).isTrue();
    verifyNoInteractions(emailApi, dispatchRepository);
  }

  @Test
  void givenValidTemplate_whenDispatch_thenSubmitEmailCalledAndSubmittedDispatchPersisted() {
    var recipient = recipient();
    var template = templateWith("Welcome", "<p>Hello</p>");

    var result = service.dispatch(recipient, template, Map.of());

    assertThat(result).isTrue();
    verify(emailApi).submitEmail(recipient.getEmail(), "Welcome", "<p>Hello</p>");
    verify(dispatchRepository).persist(dispatchCaptor.capture());
    var dispatch = dispatchCaptor.getValue();
    assertThat(dispatch.getStatusCode()).isEqualTo(NotificationDispatchStatusEnum.SUBMITTED);
    assertThat(dispatch.getError()).isNull();
    assertThat(dispatch.getRecipient()).isEqualTo(recipient);
  }

  @Test
  void givenPlaceholders_whenDispatch_thenSubjectAndBodyAreInterpolated() {
    var template = templateWith("Hello {{name}}", "<p>Dear {{name}}, your ID is {{id}}.</p>");

    service.dispatch(recipient(), template, Map.of("name", "Hans", "id", "42"));

    verify(emailApi).submitEmail(anyString(), eq("Hello Hans"), eq("<p>Dear Hans, your ID is 42.</p>"));
  }

  @Test
  void givenHtmlBody_whenDispatch_thenTextBodyStripsHtmlTags() {
    var template = templateWith("Subject", "<p>Plain <b>text</b></p>");

    service.dispatch(recipient(), template, Map.of());

    verify(emailApi).submitEmail(anyString(), anyString(), eq("<p>Plain <b>text</b></p>"));
  }

  @Test
  void givenEmailApiFails_whenDispatch_thenFailedDispatchPersistedAndReturnsFalse() {
    var recipient = recipient();
    var cause = new RuntimeException("SES error");
    doThrow(new ExternalWebServiceException("dispatch failed", cause)).when(emailApi).submitEmail(any(), any(), any());

    var result = service.dispatch(recipient, templateWith("Subject", "Body"), Map.of());

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

    service.dispatch(recipient(), templateWith("Subject", "Body"), Map.of());

    verify(dispatchRepository).persist(dispatchCaptor.capture());
    assertThat(dispatchCaptor.getValue().getError()).isEqualTo("Unknown error");
  }

  @Test
  void givenVeryLongErrorMessage_whenDispatch_thenErrorTruncatedTo1000Chars() {
    String longMessage = "e".repeat(2000);
    doThrow(new ExternalWebServiceException(longMessage, new RuntimeException("cause"))).when(emailApi).submitEmail(any(), any(), any());

    service.dispatch(recipient(), templateWith("Subject", "Body"), Map.of());

    verify(dispatchRepository).persist(dispatchCaptor.capture());
    assertThat(dispatchCaptor.getValue().getError()).hasSize(1000);
  }
}
