package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.agridata.common.exceptions.ContractTemplateOutdatedException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import org.junit.jupiter.api.Test;

class ContractTemplateVersionEnumTest {

  // Fingerprint of the XSL files of the current contract template version. Update both together with a new enum entry.
  private static final String EXPECTED_CURRENT_VERSION = "1.0";
  private static final String EXPECTED_TEMPLATE_HASH = "5a1621a9b9e3bd816f8af276d06ebdd3a2e65fafb102d42dbb036f254046fe5e";
  private static final Path TEMPLATE_DIR = Path.of("src/main/resources/pdf");

  @Test
  void givenTemplateFiles_whenHashed_thenMatchFingerprintOfCurrentVersion() throws IOException, NoSuchAlgorithmException {
    assertThat(hashTemplateFiles())
        .as("The contract template (XSL files in %s) has changed. Append a new entry to ContractTemplateVersionEnum, "
            + "then update EXPECTED_CURRENT_VERSION and EXPECTED_TEMPLATE_HASH in this test.", TEMPLATE_DIR)
        .isEqualTo(EXPECTED_TEMPLATE_HASH);
    assertThat(ContractTemplateVersionEnum.current().getVersion())
        .as("ContractTemplateVersionEnum changed. Update EXPECTED_CURRENT_VERSION and EXPECTED_TEMPLATE_HASH in this test.")
        .isEqualTo(EXPECTED_CURRENT_VERSION);
  }

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

  private static String hashTemplateFiles() throws IOException, NoSuchAlgorithmException {
    List<Path> templateFiles;
    try (var files = Files.list(TEMPLATE_DIR)) {
      templateFiles = files
          .filter(file -> file.getFileName().toString().endsWith(".xsl"))
          .sorted(Comparator.comparing(file -> file.getFileName().toString()))
          .toList();
    }
    var digest = MessageDigest.getInstance("SHA-256");
    for (var file : templateFiles) {
      digest.update(file.getFileName().toString().getBytes(StandardCharsets.UTF_8));
      // Normalize line endings so the hash is the same on Windows checkouts (core.autocrlf=true)
      var content = Files.readString(file, StandardCharsets.UTF_8).replace("\r\n", "\n");
      digest.update(content.getBytes(StandardCharsets.UTF_8));
    }
    return HexFormat.of().formatHex(digest.digest());
  }
}
