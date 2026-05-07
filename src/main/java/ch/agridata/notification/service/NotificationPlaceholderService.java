package ch.agridata.notification.service;

import ch.agridata.common.dto.TranslationDto;
import ch.agridata.common.persistence.TranslationPersistenceDto;
import ch.agridata.notification.dto.ResolvedNotificationTextsDto;
import ch.agridata.notification.persistence.NotificationBatchEntity;
import ch.agridata.notification.persistence.NotificationInboxEntity;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;

/**
 * Substitutes placeholders into values used by notification templates.
 *
 * @CommentLastReviewed 2026-05-08
 */
@ApplicationScoped
public class NotificationPlaceholderService {

  public ResolvedNotificationTextsDto resolve(NotificationBatchEntity batch) {
    var template = batch.getTemplate();
    var placeholders = batch.getPlaceholders();
    return new ResolvedNotificationTextsDto(
        applyPlaceholders(template.getWebappTitle(), placeholders),
        applyPlaceholders(template.getWebappText(), placeholders),
        applyPlaceholders(template.getEmailSubject(), placeholders),
        applyPlaceholders(template.getEmailText(), placeholders),
        applyPlaceholders(template.getMobileText(), placeholders)
    );
  }

  public ResolvedNotificationTextsDto resolve(NotificationInboxEntity inbox) {
    var batch = inbox.getRecipient().getBatch();
    return resolve(batch);
  }

  public TranslationDto applyPlaceholders(TranslationPersistenceDto translations, Map<String, String> placeholders) {
    if (translations == null) {
      return null;
    }
    return TranslationDto.builder()
        .de(substitute(translations.de(), placeholders))
        .fr(substitute(translations.fr(), placeholders))
        .it(substitute(translations.it(), placeholders))
        .build();
  }

  private static String substitute(String text, Map<String, String> placeholders) {
    if (text == null || placeholders == null || placeholders.isEmpty()) {
      return text;
    }
    String result = text;
    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
      result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
    }
    return result;
  }
}
