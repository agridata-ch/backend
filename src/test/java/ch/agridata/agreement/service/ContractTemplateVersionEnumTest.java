package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.agridata.common.exceptions.ContractTemplateOutdatedException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ContractTemplateVersionEnumTest {

  @Test
  void givenVersions_whenCurrent_thenReturnLastEntry() {
    var versions = ContractTemplateVersionEnum.values();

    assertThat(ContractTemplateVersionEnum.current()).isEqualTo(versions[versions.length - 1]);
  }

  @Test
  void givenVersions_thenVersionNumbersAreUniqueAndValidFromIsAscending() {
    var versions = ContractTemplateVersionEnum.values();

    assertThat(Arrays.stream(versions).map(ContractTemplateVersionEnum::getVersion)).doesNotHaveDuplicates();
    for (int i = 1; i < versions.length; i++) {
      assertThat(versions[i].getValidFrom()).isAfter(versions[i - 1].getValidFrom());
    }
  }

  @Test
  void givenCurrentVersion_whenVerifyIsCurrent_thenDoNotThrow() {
    assertThatCode(() -> ContractTemplateVersionEnum.verifyIsCurrent(ContractTemplateVersionEnum.current().getVersion()))
        .doesNotThrowAnyException();
  }

  @Test
  void givenOutdatedVersion_whenVerifyIsCurrent_thenThrowContractTemplateOutdatedException() {
    assertThatThrownBy(() -> ContractTemplateVersionEnum.verifyIsCurrent("0.9"))
        .isInstanceOf(ContractTemplateOutdatedException.class)
        .hasMessageContaining("0.9")
        .hasMessageContaining(ContractTemplateVersionEnum.current().getVersion());
  }
}
