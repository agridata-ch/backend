package ch.agridata.aws.service;

import ch.agridata.aws.api.SmsApi;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

/**
 * Sends SMS messages via AWS SNS.
 *
 * @CommentLastReviewed 2026-05-08
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SmsSnsService implements SmsApi {

  static final String SEND_SMS_ENABLED_PROPERTY = "agridata.aws.send-sms.enabled";

  private final SnsClient snsClient;

  @ConfigProperty(name = SEND_SMS_ENABLED_PROPERTY, defaultValue = "true")
  boolean smsSendingEnabled;

  @Override
  public void sendSms(String phoneNumber, String message) {
    phoneNumber = phoneNumber.replaceAll("\\s", "");
    // Normalize to E.164 by replacing a leading 0 with the Swiss country code (+41).
    // The application is primarily used in Switzerland and participant phone numbers are
    // sometimes stored without a country prefix, which would cause SNS to reject the request.
    if (phoneNumber.startsWith("0")) {
      phoneNumber = "+41" + phoneNumber.substring(1);
    }

    String maskedPhoneNumber = maskPhoneNumber(phoneNumber);

    if (!smsSendingEnabled) {
      log.info("SMS dispatch skipped because {}=false (recipient={})", SEND_SMS_ENABLED_PROPERTY, maskedPhoneNumber);
      return;
    }

    try {
      PublishRequest publishRequest = PublishRequest.builder()
          .phoneNumber(phoneNumber)
          .message(message)
          .build();

      PublishResponse response = snsClient.publish(publishRequest);
      log.info("SMS sent to {}. MessageID: {}", maskedPhoneNumber, response.messageId());

    } catch (SnsException e) {
      log.error("Failed to send SMS via AWS SNS to {}: {}", maskedPhoneNumber, e.awsErrorDetails().errorMessage());
      throw new ExternalWebServiceException("Service temporarily unavailable. Please try again later.", e);
    }
  }

  private String maskPhoneNumber(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.length() < 5) {
      return "****";
    }
    return phoneNumber.substring(0, 3) + "*".repeat(phoneNumber.length() - 5) + phoneNumber.substring(phoneNumber.length() - 2);
  }
}
