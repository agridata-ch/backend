package ch.agridata.product.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentDownloadDtoTest {
  private static final String FILE_NAME = "contract.pdf";
  private static final byte[] CONTENT = {1, 2, 3};

  @Test
  void givenSameValues_whenEquals_thenTrue() {
    DocumentDownloadDto a = new DocumentDownloadDto(FILE_NAME, new byte[] {1, 2, 3});
    DocumentDownloadDto b = new DocumentDownloadDto(FILE_NAME, new byte[] {1, 2, 3});

    assertThat(a).isEqualTo(b);
    assertThat(b).isEqualTo(a);
  }

  @Test
  void givenSameInstance_whenEquals_thenTrue() {
    DocumentDownloadDto dto = new DocumentDownloadDto(FILE_NAME, CONTENT);

    assertThat(dto).isEqualTo(dto);
  }

  @Test
  void givenDifferentFileName_whenEquals_thenFalse() {
    DocumentDownloadDto a = new DocumentDownloadDto("a.pdf", CONTENT);
    DocumentDownloadDto b = new DocumentDownloadDto("b.pdf", CONTENT);

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  void givenDifferentContent_whenEquals_thenFalse() {
    DocumentDownloadDto a = new DocumentDownloadDto(FILE_NAME, new byte[] {1, 2, 3});
    DocumentDownloadDto b = new DocumentDownloadDto(FILE_NAME, new byte[] {9, 9, 9});

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  void givenNullOrOtherType_whenEquals_thenFalse() {
    DocumentDownloadDto dto = new DocumentDownloadDto(FILE_NAME, CONTENT);

    assertThat(dto).isNotEqualTo(null).isNotEqualTo("some string");
  }

  @Test
  void givenBothContentsNull_whenEquals_thenTrue() {
    DocumentDownloadDto a = new DocumentDownloadDto(FILE_NAME, null);
    DocumentDownloadDto b = new DocumentDownloadDto(FILE_NAME, null);

    assertThat(a).isEqualTo(b);
  }

  @Test
  void givenOneContentNull_whenEquals_thenFalse() {
    DocumentDownloadDto a = new DocumentDownloadDto(FILE_NAME, null);
    DocumentDownloadDto b = new DocumentDownloadDto(FILE_NAME, CONTENT);

    assertThat(a).isNotEqualTo(b);
    assertThat(b).isNotEqualTo(a);
  }

  @Test
  void givenBothFileNamesNull_whenEquals_thenTrue() {
    DocumentDownloadDto a = new DocumentDownloadDto(null, new byte[] {1});
    DocumentDownloadDto b = new DocumentDownloadDto(null, new byte[] {1});

    assertThat(a).isEqualTo(b);
  }

  @Test
  void givenEqualObjects_whenHashCode_thenSameValue() {
    DocumentDownloadDto a = new DocumentDownloadDto(FILE_NAME, new byte[] {1, 2, 3});
    DocumentDownloadDto b = new DocumentDownloadDto(FILE_NAME, new byte[] {1, 2, 3});

    assertThat(a).hasSameHashCodeAs(b);
  }

  @Test
  void givenSameInstance_whenHashCode_thenConsistentAcrossInvocations() {
    DocumentDownloadDto dto = new DocumentDownloadDto(FILE_NAME, CONTENT);

    assertThat(dto).hasSameHashCodeAs(dto);
  }

  @Test
  void givenContent_whenToString_thenContainsFileNameAndContentLength() {
    DocumentDownloadDto dto = new DocumentDownloadDto(FILE_NAME, new byte[] {1, 2, 3});

    assertThat(dto).hasToString("DocumentDownloadDto[fileName=contract.pdf, contentLength=3]");
  }

  @Test
  void givenNullContent_whenToString_thenReportsZeroLength() {
    DocumentDownloadDto dto = new DocumentDownloadDto(FILE_NAME, null);

    assertThat(dto).hasToString("DocumentDownloadDto[fileName=contract.pdf, contentLength=0]");
  }
}
