package ch.agridata.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.common.persistence.TranslationPersistenceDto;
import ch.agridata.notification.persistence.NotificationBatchEntity;
import ch.agridata.notification.persistence.NotificationTemplateEntity;
import java.util.Map;
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

  @Test
  void givenValuesWithHtmlSpecialChars_whenResolve_thenEscapesEmailTextValuesButLeavesOtherChannelsUntouched() {
    var template = NotificationTemplateEntity.builder()
        .emailSubject(new TranslationPersistenceDto("Betreff {{title}}", null, null))
        .emailText(new TranslationPersistenceDto(
            "<p><strong>Antrag:</strong> {{title}} ({{consumer}})</p>", null, null))
        .webappText(new TranslationPersistenceDto("Antrag: {{title}}", null, null))
        .mobileText(new TranslationPersistenceDto("Antrag: {{title}}", null, null))
        .build();
    var batch = NotificationBatchEntity.builder()
        .template(template)
        .placeholders(Map.of("title", "Bio & Co <5%>", "consumer", "A \"B\" 'C'"))
        .build();

    var resolved = service.resolve(batch);

    assertThat(resolved.emailText().de())
        .isEqualTo("<p><strong>Antrag:</strong> Bio &amp; Co &lt;5%&gt; (A &quot;B&quot; &#39;C&#39;)</p>");
    assertThat(resolved.emailSubject().de()).isEqualTo("Betreff Bio & Co <5%>");
    assertThat(resolved.webappText().de()).isEqualTo("Antrag: Bio & Co <5%>");
    assertThat(resolved.mobileText().de()).isEqualTo("Antrag: Bio & Co <5%>");
  }

  @Test
  void givenAllHtmlSpecialCharacters_whenResolve_thenEscapesAllCorrectly() {
    var template = NotificationTemplateEntity.builder()
        .emailText(new TranslationPersistenceDto("{{ampersand}} {{lessThan}} {{greaterThan}} {{quote}} {{apostrophe}}", null, null))
        .build();
    var batch = NotificationBatchEntity.builder()
        .template(template)
        .placeholders(Map.of(
            "ampersand", "&",
            "lessThan", "<",
            "greaterThan", ">",
            "quote", "\"",
            "apostrophe", "'"
        ))
        .build();

    var resolved = service.resolve(batch);

    assertThat(resolved.emailText().de())
        .isEqualTo("&amp; &lt; &gt; &quot; &#39;");
  }

  @Test
  void givenPlaceholderWithHtmlContent_whenResolveEmailText_thenEscapesProperlyForHtml() {
    var template = NotificationTemplateEntity.builder()
        .emailText(new TranslationPersistenceDto("Value: {{html}}", null, null))
        .build();
    var batch = NotificationBatchEntity.builder()
        .template(template)
        .placeholders(Map.of("html", "<div>test</div>"))
        .build();

    var resolved = service.resolve(batch);

    // htmlEscape converts HTML tags to entities
    assertThat(resolved.emailText().de()).isEqualTo("Value: &lt;div&gt;test&lt;/div&gt;");
  }
}
