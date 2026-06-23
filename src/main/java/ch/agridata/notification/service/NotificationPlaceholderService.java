package ch.agridata.notification.service;

import ch.agridata.common.dto.TranslationDto;
import ch.agridata.common.persistence.TranslationPersistenceDto;
import ch.agridata.notification.dto.ResolvedNotificationTextsDto;
import ch.agridata.notification.persistence.NotificationBatchEntity;
import ch.agridata.notification.persistence.NotificationTemplateEntity;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Substitutes placeholders into values used by notification templates.
 *
 * @CommentLastReviewed 2026-05-18
 */
@ApplicationScoped
public class NotificationPlaceholderService {

  private static final String PLACEHOLDER_OPEN = "{{";
  private static final String PLACEHOLDER_CLOSE = "}}";

  public ResolvedNotificationTextsDto resolve(NotificationBatchEntity batch) {
    var template = batch.getTemplate();
    var placeholders = batch.getPlaceholders();
    return new ResolvedNotificationTextsDto(
        applyPlaceholders(template.getWebappTitle(), placeholders),
        applyPlaceholders(template.getWebappText(), placeholders),
        applyPlaceholders(template.getEmailSubject(), placeholders),
        applyHtmlPlaceholders(template.getEmailText(), placeholders),
        applyPlaceholders(template.getMobileText(), placeholders)
    );
  }

  private TranslationDto applyPlaceholders(TranslationPersistenceDto translations, Map<String, String> placeholders) {
    if (translations == null) {
      return null;
    }
    return TranslationDto.builder()
        .de(substitute(translations.de(), placeholders, false))
        .fr(substitute(translations.fr(), placeholders, false))
        .it(substitute(translations.it(), placeholders, false))
        .build();
  }

  private TranslationDto applyHtmlPlaceholders(TranslationPersistenceDto translations, Map<String, String> placeholders) {
    if (translations == null) {
      return null;
    }
    return TranslationDto.builder()
        .de(substitute(translations.de(), placeholders, true))
        .fr(substitute(translations.fr(), placeholders, true))
        .it(substitute(translations.it(), placeholders, true))
        .build();
  }

  /**
   * Collects all placeholder names referenced via {@code {{name}}} in the template's
   * {@code emailSubject}, {@code emailText}, {@code webappText} and {@code mobileText}.
   */
  public Set<String> extractRequiredPlaceholders(NotificationTemplateEntity template) {
    Set<String> names = new HashSet<>();
    Stream.of(template.getEmailSubject(), template.getEmailText(), template.getWebappText(), template.getMobileText())
        .forEach(translation -> collectPlaceholderNames(translation, names));
    return names;
  }

  private static void collectPlaceholderNames(TranslationPersistenceDto translation, Set<String> sink) {
    if (translation == null) {
      return;
    }
    Stream.of(translation.de(), translation.fr(), translation.it())
        .filter(text -> text != null && !text.isBlank())
        .forEach(text -> extractPlaceholderNames(text, sink));
  }

  /**
   * Optimized extraction using string operations (indexOf, substring) instead of regex.
   * Finds all {{placeholderName}} patterns and adds extracted names to sink.
   */
  private static void extractPlaceholderNames(String text, Set<String> sink) {
    int index = 0;
    while ((index = text.indexOf(PLACEHOLDER_OPEN, index)) != -1) {
      int endIndex = text.indexOf(PLACEHOLDER_CLOSE, index + 2);
      if (endIndex != -1) {
        String placeholder = text.substring(index + 2, endIndex).trim();
        sink.add(placeholder);
        index = endIndex + 2;
      } else {
        break;
      }
    }
  }

  private static String substitute(String text, Map<String, String> placeholders, boolean htmlEscapeValues) {
    if (text == null || placeholders == null || placeholders.isEmpty()) {
      return text;
    }
    String result = text;
    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
      String value = htmlEscapeValues ? htmlEscape(entry.getValue()) : entry.getValue();
      result = result.replace("{{" + entry.getKey() + "}}", value);
    }
    return result;
  }

  private static String htmlEscape(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }
}
