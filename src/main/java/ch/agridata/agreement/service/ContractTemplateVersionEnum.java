package ch.agridata.agreement.service;

import ch.agridata.common.exceptions.ContractTemplateOutdatedException;
import java.time.LocalDate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Lists all versions of the contract template (XSL-FO files in src/main/resources/pdf). The last entry is the current
 * version, i.e. the one matching the deployed XSL files. When changing the template, append a new entry in the same commit.
 * Entries must never be removed or reordered, because existing contract revisions reference their version.
 *
 * @CommentLastReviewed 2026-11-13
 */
@Getter
@RequiredArgsConstructor
public enum ContractTemplateVersionEnum {
  V1_0("1.0", LocalDate.of(2026, 3, 16));

  private final String version;
  private final LocalDate validFrom;

  public static ContractTemplateVersionEnum current() {
    var versions = values();
    return versions[versions.length - 1];
  }

  public static void verifyIsCurrent(String contractTemplateVersion) {
    var currentVersion = current().getVersion();
    if (!currentVersion.equals(contractTemplateVersion)) {
      throw new ContractTemplateOutdatedException(
          "Contract was created with contract template version " + contractTemplateVersion
              + ", but the current version is " + currentVersion
              + ". The data request must be withdrawn and resubmitted."
      );
    }
  }
}
