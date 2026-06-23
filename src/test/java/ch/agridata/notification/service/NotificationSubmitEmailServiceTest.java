package ch.agridata.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import ch.agridata.aws.api.EmailApi;
import ch.agridata.common.dto.SupportedLanguage;
import ch.agridata.common.dto.TranslationDto;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.notification.dto.ResolvedNotificationTextsDto;
import ch.agridata.notification.persistence.NotificationDispatchEntity;
import ch.agridata.notification.persistence.NotificationDispatchRepository;
import ch.agridata.notification.persistence.NotificationDispatchStatusEnum;
import ch.agridata.notification.persistence.NotificationRecipientEntity;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
 * @CommentLastReviewed 2026-05-18
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

  @BeforeEach
  void setUp() {
    service.loadEmailTemplate();
  }

  // ── fixtures ─────────────────────────────────────────────────────────────

  private static NotificationRecipientEntity recipient() {
    return NotificationRecipientEntity.builder().id(UUID.randomUUID()).email("test@example.com").build();
  }

  private static NotificationRecipientEntity recipientWithLanguage(SupportedLanguage language) {
    return NotificationRecipientEntity.builder().id(UUID.randomUUID()).email("test@example.com").language(language).build();
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
    verify(emailApi).submitEmail(eq(recipient.getEmail()), eq("Welcome"), argThat(body -> body.contains("<p>Hello</p>")));
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

    verify(emailApi).submitEmail(anyString(), eq("Hallo Hans"), argThat(body -> body.contains("<p>Lieber Hans, deine ID ist 42.</p>")));
  }

  @Test
  void givenMultilingualEmailTextAndNoRecipientLanguage_whenDispatch_thenBodyContainsOnlyGermanPart() {
    var resolved = new ResolvedNotificationTextsDto(
        null,
        null,
        TranslationDto.builder().de("Betreff").build(),
        TranslationDto.builder().de("<p>DE text</p>").fr("<p>FR text</p>").it("<p>IT text</p>").build(),
        null
    );

    service.dispatch(recipient(), resolved);

    verify(emailApi).submitEmail(
        anyString(), anyString(),
        argThat(body -> body.contains("<p>DE text</p>")
            && !body.contains("<p>FR text</p>")
            && !body.contains("<p>IT text</p>")
            && !body.contains("<hr"))
    );
  }

  @Test
  void givenSingleLanguagePart_whenDispatch_thenBodyContainsNoSeparator() {
    var resolved = new ResolvedNotificationTextsDto(
        null,
        null,
        TranslationDto.builder().de("Betreff").build(),
        TranslationDto.builder().de("<p>DE only</p>").build(),
        null
    );

    service.dispatch(recipient(), resolved);

    verify(emailApi).submitEmail(anyString(), anyString(), argThat(body -> body.contains("<p>DE only</p>") && !body.contains("<hr")));
  }

  @Test
  void givenNullLanguageParts_whenDispatch_thenNullPartsOmittedFromBody() {
    var resolved = new ResolvedNotificationTextsDto(
        null,
        null,
        TranslationDto.builder().de("Betreff").build(),
        TranslationDto.builder().de("<p>DE only</p>").fr(null).it(null).build(),
        null
    );

    service.dispatch(recipient(), resolved);

    verify(emailApi).submitEmail(
        anyString(), anyString(),
        argThat(body -> body.contains("<p>DE only</p>") && !body.contains("null"))
    );
  }

  @Test
  void givenBlankLanguageParts_whenDispatch_thenBlankPartsOmittedFromBody() {
    var resolved = new ResolvedNotificationTextsDto(
        null,
        null,
        TranslationDto.builder().de("Betreff").build(),
        TranslationDto.builder().de("<p>DE only</p>").fr("   ").it("").build(),
        null
    );

    service.dispatch(recipient(), resolved);

    verify(emailApi).submitEmail(
        anyString(), anyString(),
        argThat(body -> body.contains("<p>DE only</p>")
            && body.indexOf("<p>DE only</p>") == body.lastIndexOf("<p>DE only</p>"))
    );
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

  // ── language-specific email ───────────────────────────────────────────────

  @Test
  void givenRecipientWithLanguageFr_whenDispatch_thenSubjectAndBodyInFrenchOnly() {
    var resolved = new ResolvedNotificationTextsDto(
        null,
        null,
        TranslationDto.builder().de("Betreff").fr("Objet").it("Oggetto").build(),
        TranslationDto.builder().de("<p>DE text</p>").fr("<p>FR text</p>").it("<p>IT text</p>").build(),
        null
    );

    service.dispatch(recipientWithLanguage(SupportedLanguage.FR), resolved);

    verify(emailApi).submitEmail(
        anyString(),
        eq("Objet"),
        argThat(body -> body.contains("<p>FR text</p>") && !body.contains("<p>DE text</p>") && !body.contains("<hr"))
    );
  }

  @Test
  void givenRecipientWithLanguageDe_whenDispatch_thenSubjectAndBodyInGermanOnly() {
    var resolved = new ResolvedNotificationTextsDto(
        null,
        null,
        TranslationDto.builder().de("Betreff").fr("Objet").it("Oggetto").build(),
        TranslationDto.builder().de("<p>DE text</p>").fr("<p>FR text</p>").it("<p>IT text</p>").build(),
        null
    );

    service.dispatch(recipientWithLanguage(SupportedLanguage.DE), resolved);

    verify(emailApi).submitEmail(
        anyString(),
        eq("Betreff"),
        argThat(body -> body.contains("<p>DE text</p>") && !body.contains("<p>FR text</p>") && !body.contains("<hr"))
    );
  }

  @Test
  void givenRecipientWithLanguageItButMissingItTranslation_whenDispatch_thenFallsBackToDe() {
    var resolved = new ResolvedNotificationTextsDto(
        null,
        null,
        TranslationDto.builder().de("Betreff").fr("Objet").build(),
        TranslationDto.builder().de("<p>DE text</p>").fr("<p>FR text</p>").build(),
        null
    );

    service.dispatch(recipientWithLanguage(SupportedLanguage.IT), resolved);

    verify(emailApi).submitEmail(
        anyString(),
        eq("Betreff"),
        argThat(body -> body.contains("<p>DE text</p>") && !body.contains("<hr"))
    );
  }

  // ── loadEmailTemplate error handling ──────────────────────────────────────

  @Test
  void whenTemplateResourceNotFound_thenLoadEmailTemplateThrowsIllegalStateException() {
    var svc = spy(new NotificationSubmitEmailService(emailApi, dispatchRepository));
    doReturn(null).when(svc).openTemplateResource();

    assertThatThrownBy(svc::loadEmailTemplate)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Email template not found on classpath");
  }

  @Test
  void whenTemplateInputStreamThrowsIOException_thenLoadEmailTemplateThrowsIllegalStateException() throws IOException {
    var brokenStream = mock(InputStream.class);
    doThrow(new IOException("disk error")).when(brokenStream).readAllBytes();

    var svc = spy(new NotificationSubmitEmailService(emailApi, dispatchRepository));
    doReturn(brokenStream).when(svc).openTemplateResource();

    assertThatThrownBy(svc::loadEmailTemplate)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Failed to load email template")
        .hasCauseInstanceOf(IOException.class);
  }

  @Test
  void givenValidEmailTemplate_whenLoadEmailTemplate_thenTemplateSuccessfullyLoadedAsUtf8String() {
    service.loadEmailTemplate();

    // Verify that the template was loaded successfully by dispatching an email
    var recipient = recipient();
    var resolved = resolvedWith("Test Subject", "<p>Template loaded successfully</p>");

    var result = service.dispatch(recipient, resolved);

    assertThat(result).isTrue();
    verify(emailApi).submitEmail(anyString(), anyString(), argThat(body -> body.contains("Template loaded successfully")));
  }
}
