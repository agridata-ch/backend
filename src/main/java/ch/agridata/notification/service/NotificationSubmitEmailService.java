package ch.agridata.notification.service;

import ch.agridata.aws.api.EmailApi;
import ch.agridata.notification.dto.ResolvedNotificationTextsDto;
import ch.agridata.notification.persistence.NotificationChannelCodeEnum;
import ch.agridata.notification.persistence.NotificationDispatchEntity;
import ch.agridata.notification.persistence.NotificationDispatchRepository;
import ch.agridata.notification.persistence.NotificationDispatchStatusEnum;
import ch.agridata.notification.persistence.NotificationRecipientEntity;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Submits emails to be sent to a recipient using the configured {@link EmailApi} and
 * records the outcome as a {@link NotificationDispatchEntity}.
 *
 * @CommentLastReviewed 2026-05-08
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class NotificationSubmitEmailService {

  private final EmailApi emailApi;
  private final NotificationDispatchRepository dispatchRepository;

  public boolean dispatch(NotificationRecipientEntity recipient, ResolvedNotificationTextsDto resolvedNotificationTexts) {

    if (resolvedNotificationTexts.emailSubject() == null || resolvedNotificationTexts.emailText() == null) {
      log.warn("Resolved notification has no email content configured; skipping email for recipient {}.", recipient.getId());
      return true;
    }

    String subject = resolvedNotificationTexts.emailSubject().de();
    String body = resolvedNotificationTexts.emailText().de();

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
}
