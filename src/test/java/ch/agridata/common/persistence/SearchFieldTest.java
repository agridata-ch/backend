package ch.agridata.common.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.common.dto.SupportedLanguage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

class SearchFieldTest {

  @Test
  @DisplayName("simple() creates a non-translated field")
  void simpleFieldIsNotTranslated() {
    var field = SearchField.simple("code");

    assertThat(field.path()).isEqualTo("code");
    assertThat(field.translated()).isFalse();
  }

  @Test
  @DisplayName("translated() creates a translated field")
  void translatedFieldIsTranslated() {
    var field = SearchField.translated("dp.name");

    assertThat(field.path()).isEqualTo("dp.name");
    assertThat(field.translated()).isTrue();
  }

  @Test
  @DisplayName("toHql on a simple field returns the raw path, independent of language")
  void toHqlSimpleReturnsRawPath() {
    var field = SearchField.simple("code");

    assertThat(field.toHql(SupportedLanguage.DE)).isEqualTo("code");
    assertThat(field.toHql(SupportedLanguage.FR)).isEqualTo("code");
    assertThat(field.toHql(SupportedLanguage.IT)).isEqualTo("code");
  }

  @Test
  @DisplayName("toHql on a translated field extracts the JSON value for the request language")
  void toHqlTranslatedResolvesLanguage() {
    var field = SearchField.translated("dp.name");

    assertThat(field.toHql(SupportedLanguage.DE))
        .isEqualTo("function('jsonb_extract_path_text', dp.name, 'de')");
    assertThat(field.toHql(SupportedLanguage.FR))
        .isEqualTo("function('jsonb_extract_path_text', dp.name, 'fr')");
    assertThat(field.toHql(SupportedLanguage.IT))
        .isEqualTo("function('jsonb_extract_path_text', dp.name, 'it')");
  }
}
