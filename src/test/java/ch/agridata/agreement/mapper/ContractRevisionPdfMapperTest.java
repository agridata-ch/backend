package ch.agridata.agreement.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.common.persistence.TranslationPersistenceDto;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

/**
 * Unit tests for {@link ContractRevisionPdfMapper}, covering the rich-text purpose mapping that parses each language's HTML into DOM
 * elements for verbatim marshalling into the PDF source tree.
 *
 * @CommentLastReviewed 2026-09-23
 */
class ContractRevisionPdfMapperTest {

  private final ContractRevisionPdfMapper mapper = new ContractRevisionPdfMapperImpl();

  @Test
  void givenNull_toContractRevisionPdfPurposeDto_returnsNull() {
    assertThat(mapper.toContractRevisionPdfPurposeDto(null)).isNull();
  }

  @Test
  void givenTranslations_toContractRevisionPdfPurposeDto_parsesEachLanguage() {
    var translation = TranslationPersistenceDto.builder()
        .de("<p>Zweck</p>")
        .fr("<p>But</p>")
        .it("<p>Scopo</p>")
        .build();

    var purpose = mapper.toContractRevisionPdfPurposeDto(translation);

    assertThat(purpose).isNotNull();
    assertThat(purpose.getLanguages())
        .extracting(Element::getTagName)
        .containsExactly("de", "fr", "it");
    assertThat(purpose.getLanguages())
        .allSatisfy(element -> assertThat(element.getElementsByTagName("p").getLength()).isEqualTo(1));
  }
}