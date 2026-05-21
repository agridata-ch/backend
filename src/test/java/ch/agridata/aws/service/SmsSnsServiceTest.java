package ch.agridata.aws.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

@ExtendWith(MockitoExtension.class)
class SmsSnsServiceTest {

  @Mock
  private SnsClient snsClient;

  @Captor
  private ArgumentCaptor<PublishRequest> publishRequestCaptor;

  @InjectMocks
  private SmsSnsService smsSnsService;

  private static final String PHONE = "+41791234567";
  private static final String MESSAGE = "Sicherheitscode / Code Sécuritaire / Codice di sicurezza: 123456";

  @BeforeEach
  void setUp() {
    smsSnsService.smsSendingEnabled = true;
  }

  @Test
  void givenValidRequest_whenSendSms_thenPublishCalledWithCorrectData() {
    when(snsClient.publish(any(PublishRequest.class)))
        .thenReturn(PublishResponse.builder().messageId("mock-msg-id").build());

    smsSnsService.sendSms(PHONE, MESSAGE);

    verify(snsClient).publish(publishRequestCaptor.capture());
    PublishRequest capturedRequest = publishRequestCaptor.getValue();

    assertThat(capturedRequest.phoneNumber()).isEqualTo(PHONE);
    assertThat(capturedRequest.message()).isEqualTo(MESSAGE);
  }

  @Test
  void givenPhoneWithWhitespace_whenSendSms_thenWhitespaceRemoved() {
    when(snsClient.publish(any(PublishRequest.class)))
        .thenReturn(PublishResponse.builder().messageId("mock-msg-id").build());

    smsSnsService.sendSms("+41 79 123 45 67", MESSAGE);

    verify(snsClient).publish(publishRequestCaptor.capture());
    assertThat(publishRequestCaptor.getValue().phoneNumber()).isEqualTo("+41791234567");
  }

  @Test
  void givenPhoneWithLeadingZero_whenSendSms_thenNumberNormalizedToE164() {
    when(snsClient.publish(any(PublishRequest.class)))
        .thenReturn(PublishResponse.builder().messageId("mock-msg-id").build());

    smsSnsService.sendSms("0791234567", MESSAGE);

    verify(snsClient).publish(publishRequestCaptor.capture());
    assertThat(publishRequestCaptor.getValue().phoneNumber()).isEqualTo("+41791234567");
  }

  @Test
  void givenSnsThrowsException_whenSendSms_thenThrowExternalWebServiceException() {
    AwsErrorDetails errorDetails = AwsErrorDetails.builder()
        .errorMessage("Invalid credentials")
        .errorCode("403")
        .build();
    SnsException snsException = (SnsException) SnsException.builder()
        .message("AWS Error")
        .awsErrorDetails(errorDetails)
        .build();
    when(snsClient.publish(any(PublishRequest.class))).thenThrow(snsException);

    assertThatThrownBy(() -> smsSnsService.sendSms(PHONE, MESSAGE))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("Service temporarily unavailable");
  }

  @Test
  void givenSendSmsDisabled_whenSendSms_thenNoPublishAndNoThrow() {
    smsSnsService.smsSendingEnabled = false;

    assertThatCode(() -> smsSnsService.sendSms(PHONE, MESSAGE)).doesNotThrowAnyException();

    verify(snsClient, never()).publish(any(PublishRequest.class));
    verifyNoInteractions(snsClient);
  }
}
