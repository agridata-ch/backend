package ch.agridata.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.common.persistence.TranslationPersistenceDto;
import ch.agridata.notification.persistence.NotificationTemplateEntity;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link NotificationPlaceholderService}.
 *
 * @CommentLastReviewed 2026-05-18
 */
class NotificationPlaceholderServiceTest {

  private final NotificationPlaceholderService service = new NotificationPlaceholderService();

  @Test
  void givenTemplateWithPlaceholders_whenExtractRequiredPlaceholders_thenReturnsDeduplicatedNamesAcrossAllTextFields() {
    var template = NotificationTemplateEntity.builder()
        .emailSubject(new TranslationPersistenceDto("Antrag {{dataConsumer}}", "Demande {{dataConsumer}}", "Richiesta {{dataConsumer}}"))
        .emailText(new TranslationPersistenceDto("Titel: {{dataRequestTitleDe}} → {{dataRequestUrl}}", null, null))
        .webappText(new TranslationPersistenceDto("{{dataRequestTitleDe}}", "{{dataRequestTitleFr}}", "{{dataRequestTitleIt}}"))
        .mobileText(new TranslationPersistenceDto("kurz: {{dataConsumer}}", null, null))
        .build();

    var result = service.extractRequiredPlaceholders(template);

    assertThat(result).containsExactlyInAnyOrder(
        "dataConsumer", "dataRequestTitleDe", "dataRequestUrl", "dataRequestTitleFr", "dataRequestTitleIt");
  }

  @Test
  void givenNullTextFields_whenExtractRequiredPlaceholders_thenIgnoresThem() {
    var template = NotificationTemplateEntity.builder()
        .emailText(new TranslationPersistenceDto("{{only}}", null, null))
        .build();

    var result = service.extractRequiredPlaceholders(template);

    assertThat(result).containsExactly("only");
  }

  @Test
  void givenTemplateWithoutPlaceholders_whenExtractRequiredPlaceholders_thenReturnsEmptySet() {
    var template = NotificationTemplateEntity.builder()
        .emailSubject(new TranslationPersistenceDto("Statischer Betreff", "Sujet statique", "Oggetto statico"))
        .build();

    var result = service.extractRequiredPlaceholders(template);

    assertThat(result).isEmpty();
  }

  @Test
  void givenPlaceholderWithWhitespace_whenExtractRequiredPlaceholders_thenStripsAndCapturesName() {
    var template = NotificationTemplateEntity.builder()
        .emailText(new TranslationPersistenceDto("Hallo {{  name  }}", null, null))
        .build();

    var result = service.extractRequiredPlaceholders(template);

    assertThat(result).containsExactly("name");
  }
}
