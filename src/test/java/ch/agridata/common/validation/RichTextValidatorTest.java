package ch.agridata.common.validation;

import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.dto.DataRequestPurposeDto;
import ch.agridata.agreement.dto.DataRequestUpdateDto;
import ch.agridata.product.dto.DataProductDescriptionDto;
import ch.agridata.product.dto.DataProductUpdateDto;
import ch.agridata.user.dto.AgbRevisionCreateDto;
import ch.agridata.user.dto.AgbTextDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Unit tests for {@link RichTextValidator}: the jsoup allowlist itself and its wiring on the rich-text fields of the update DTOs.
 *
 * @CommentLastReviewed 2026-11-12
 */
class RichTextValidatorTest {

  private static final Safelist EDITOR = RichTextValidator.safelist(false);
  private static final Safelist WITH_HEADINGS = RichTextValidator.safelist(true);

  private static ValidatorFactory validatorFactory;
  private static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    validatorFactory = Validation.byDefaultProvider().configure()
        .addValueExtractor(new JsonNullableValueExtractor())
        .buildValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @AfterAll
  static void tearDownValidator() {
    validatorFactory.close();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "<p>Hello <strong>bold</strong>, <em>italic</em> and <u>underlined</u></p>",
      "<ul><li><p>one</p></li></ul><ol><li><p>two</p></li></ol>",
      "<ol start=\"3\"><li><p>three</p></li></ol>",
      "<p>line<br>break&nbsp;and &lt;escaped&gt; &amp; text</p>",
      "<p></p>",
      "plain text",
      ""
  })
  void givenEditorOutput_whenValidated_thenValid(String html) {
    assertThat(RichTextValidator.isValidHtml(html, EDITOR)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "<script>alert(1)</script>",
      "<p onclick=\"alert(1)\">x</p>",
      "<a href=\"javascript:alert(1)\">x</a>",
      "<img src=\"x\" onerror=\"alert(1)\">",
      "<p style=\"color:red\">x</p>",
      "<iframe src=\"https://example.com\"></iframe>",
      "<h1>x</h1>",
      "<ol type=\"a\"><li>x</li></ol>",
      "<ul start=\"3\"><li>x</li></ul>"
  })
  void givenMarkupOutsideAllowlist_whenValidated_thenInvalid(String html) {
    assertThat(RichTextValidator.isValidHtml(html, EDITOR)).isFalse();
    assertThat(RichTextValidator.isValidHtml(html, WITH_HEADINGS)).isEqualTo(html.startsWith("<h1>"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"<h1>AGB</h1>", "<h2>1. Geltungsbereich</h2><p>Text</p>", "<h6>x</h6>"})
  void givenHeadings_whenValidated_thenOnlyValidIfHeadingsAllowed(String html) {
    assertThat(RichTextValidator.isValidHtml(html, EDITOR)).isFalse();
    assertThat(RichTextValidator.isValidHtml(html, WITH_HEADINGS)).isTrue();
  }

  @Test
  void givenAgbRevisionWithHeadings_whenValidate_thenValid() {
    var dto = new AgbRevisionCreateDto("1.5", null, new AgbTextDto("<h1>AGB</h1><h2>1.</h2><p>Text</p>", "<h1>CG</h1>", "<h1>CG</h1>"));

    assertThat(validator.validate(dto)).isEmpty();
  }

  @Test
  void givenAgbRevisionWithScript_whenValidate_thenViolationOnLanguage() {
    var dto = new AgbRevisionCreateDto("1.5", null, new AgbTextDto("<h1>AGB</h1>", "<h1>CG</h1><script>alert(1)</script>", null));

    assertThat(richTextViolations(validator.validate(dto)))
        .singleElement()
        .satisfies(v -> assertThat(v.getPropertyPath()).hasToString("agbText.fr"));
  }

  @Test
  void givenNull_whenValidated_thenValid() {
    assertThat(RichTextValidator.isValidHtml(null, EDITOR)).isTrue();
    assertThat(new RichTextValidator().isValid(null, null)).isTrue();
  }

  @Test
  void givenDataRequestPurposeWithScript_whenValidate_thenViolationOnLanguage() {
    var dto = DataRequestUpdateDto.builder()
        .purpose(new DataRequestPurposeDto("<p>gut</p>", "<p>bon</p><script>alert(1)</script>", null))
        .build();

    assertThat(richTextViolations(validator.validate(dto)))
        .singleElement()
        .satisfies(v -> assertThat(v.getPropertyPath()).hasToString("purpose.fr"));
  }

  @Test
  void givenDataProductPricingBasisWithScript_whenValidate_thenViolation() {
    var dto = DataProductUpdateDto.builder()
        .pricingBasis(JsonNullable.of(new DataProductDescriptionDto("<p onclick=\"x\">a</p>", null, null)))
        .build();

    assertThat(richTextViolations(validator.validate(dto)))
        .singleElement()
        .satisfies(v -> assertThat(v.getPropertyPath().toString()).startsWith("pricingBasis").endsWith("de"));
  }

  @Test
  void givenPlainTextDescriptionSharingPricingBasisDto_whenValidate_thenNotCheckedAsRichText() {
    var dto = DataProductUpdateDto.builder()
        .description(JsonNullable.of(new DataProductDescriptionDto("<b>not rich text</b>", null, null)))
        .build();

    assertThat(richTextViolations(validator.validate(dto))).isEmpty();
  }

  private static <T> List<ConstraintViolation<T>> richTextViolations(Set<ConstraintViolation<T>> violations) {
    return violations.stream()
        .filter(v -> v.getConstraintDescriptor().getAnnotation() instanceof RichText)
        .toList();
  }
}
