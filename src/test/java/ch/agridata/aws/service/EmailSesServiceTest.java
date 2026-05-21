package ch.agridata.aws.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.common.exceptions.ExternalWebServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.SesException;

/**
 * Unit tests for {@link EmailSesService}.
 *
 * @CommentLastReviewed 2026-05-06
 */
@ExtendWith(MockitoExtension.class)
class EmailSesServiceTest {

  @Mock
  private SesClient sesClient;

  @Captor
  private ArgumentCaptor<SendEmailRequest> sendEmailRequestCaptor;

  @InjectMocks
  private EmailSesService emailSesService;

  private static final String TO = "recipient@example.com";
  private static final String SUBJECT = "Willkommen bei agridata.ch";
  private static final String HTML_BODY = "<p>Hallo</p>";

  @BeforeEach
  void setUp() {
    emailSesService.senderAddress = "no-reply@agridata.ch";
    emailSesService.emailEnabled = true;
    emailSesService.activeProfile = "prod";
  }

  @Test
  void givenValidRequest_whenSendEmail_thenSubmitEmailCalledWithCorrectData() {
    when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(SendEmailResponse.builder().messageId("mock-msg-id").build());

    emailSesService.submitEmail(TO, SUBJECT, HTML_BODY);

    verify(sesClient).sendEmail(sendEmailRequestCaptor.capture());
    SendEmailRequest captured = sendEmailRequestCaptor.getValue();

    assertThat(captured.source()).isEqualTo("no-reply@agridata.ch");
    assertThat(captured.destination().toAddresses()).containsExactly(TO);
    assertThat(captured.message().subject().data()).isEqualTo(SUBJECT);
    assertThat(captured.message().body().html().data()).isEqualTo(HTML_BODY);
  }

  @Test
  void givenSesThrowsException_whenSubmitEmail_thenThrowExternalWebServiceException() {
    AwsErrorDetails errorDetails = AwsErrorDetails.builder().errorMessage("Message rejected").errorCode("MessageRejected").build();
    SesException sesException = (SesException) SesException.builder().message("AWS Error").awsErrorDetails(errorDetails).build();
    when(sesClient.sendEmail(any(SendEmailRequest.class))).thenThrow(sesException);

    assertThatThrownBy(() -> emailSesService.submitEmail(TO, SUBJECT, HTML_BODY)).isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("Failed to submit email. Error: AwsErrorDetails(errorMessage=Message rejected, errorCode=MessageRejected)");
  }

  @Test
  void givenEmailDisabled_whenSubmitEmail_Feature_thenSesNotCalled() {
    emailSesService.emailEnabled = false;

    emailSesService.submitEmail(TO, SUBJECT, HTML_BODY);

    verify(sesClient, never()).sendEmail(any(SendEmailRequest.class));
  }

  @Test
  void givenNonProdProfileAndDisallowedRecipient_whenSubmitEmail_thenSesNotCalled() {
    emailSesService.activeProfile = "local";

    emailSesService.submitEmail(TO, SUBJECT, HTML_BODY);

    verify(sesClient, never()).sendEmail(any(SendEmailRequest.class));
  }

  @Test
  void givenNonProdProfileAndAllowedRecipient_whenSubmitEmail_thenSesIsCalled() {
    emailSesService.activeProfile = "local";
    when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(SendEmailResponse.builder().messageId("mock-msg-id").build());

    emailSesService.submitEmail("example@" + EmailSesService.ALLOWED_EMAIL_DOMAINS_NON_PROD.getFirst(), SUBJECT, HTML_BODY);

    verify(sesClient).sendEmail(any(SendEmailRequest.class));
  }

  @Test
  void givenNonProdProfileAndDisallowedRecipient_butEmailDisabled_whenSubmitEmail_Feature_thenSesNotCalled() {
    emailSesService.activeProfile = "develop";
    emailSesService.emailEnabled = false;

    emailSesService.submitEmail("example@" + EmailSesService.ALLOWED_EMAIL_DOMAINS_NON_PROD.getFirst(), SUBJECT, HTML_BODY);

    verify(sesClient, never()).sendEmail(any(SendEmailRequest.class));
  }
}
