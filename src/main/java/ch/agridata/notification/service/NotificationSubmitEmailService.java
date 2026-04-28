package ch.agridata.notification.service;

import ch.agridata.aws.api.EmailApi;
import ch.agridata.notification.persistence.NotificationChannelCodeEnum;
import ch.agridata.notification.persistence.NotificationDispatchEntity;
import ch.agridata.notification.persistence.NotificationDispatchRepository;
import ch.agridata.notification.persistence.NotificationDispatchStatusEnum;
import ch.agridata.notification.persistence.NotificationRecipientEntity;
import ch.agridata.notification.persistence.NotificationTemplateEntity;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Submits emails to be sent to a recipient using the configured {@link EmailApi} and
 * records the outcome as a {@link NotificationDispatchEntity}.
 *
 * @CommentLastReviewed 2026-04-28
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class NotificationSubmitEmailService {

  private final EmailApi emailApi;
  private final NotificationDispatchRepository dispatchRepository;

  public boolean dispatch(
      NotificationRecipientEntity recipient,
      NotificationTemplateEntity template,
      Map<String, String> genericPlaceholders
  ) {

    if (template.getEmailSubject() == null || template.getEmailText() == null) {
      log.warn("Template {} has no email content configured; skipping email for recipient {}.", template.getId(), recipient.getId());
      return true;
    }

    String subject = applyPlaceholders(template.getEmailSubject().de(), genericPlaceholders);
    String body = applyPlaceholders(template.getEmailText().de(), genericPlaceholders);

    try {
      emailApi.submitEmail(recipient.getEmail(), subject, body);
      createSuccessfulEmailDispatchEntry(recipient);
      return true;

    } catch (Exception e) {
      log.error("Failed to send email to recipient {}: {}", recipient.getId(), e.getCause().getMessage());
      String error = e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 1000)) : "Unknown error";
      createEmailDispatchEntry(recipient, NotificationDispatchStatusEnum.FAILED, error);
      return false;
    }
  }

  private void createSuccessfulEmailDispatchEntry(NotificationRecipientEntity recipient) {
    createEmailDispatchEntry(recipient, NotificationDispatchStatusEnum.SUBMITTED, null);
  }

  private void createEmailDispatchEntry(NotificationRecipientEntity recipient, NotificationDispatchStatusEnum statusCode, String error) {
    var notificationDispatchEntity = NotificationDispatchEntity.builder()
        .recipient(recipient)
        .channelCode(NotificationChannelCodeEnum.EMAIL)
        .statusCode(statusCode)
        .error(error)
        .build();
    dispatchRepository.persist(notificationDispatchEntity);
  }

  private static String applyPlaceholders(String text, Map<String, String> placeholders) {
    if (text == null || placeholders == null || placeholders.isEmpty()) {
      return text;
    }
    for (var entry : placeholders.entrySet()) {
      text = text.replace("{{" + entry.getKey() + "}}", entry.getValue());
    }
    return text;
  }
}
