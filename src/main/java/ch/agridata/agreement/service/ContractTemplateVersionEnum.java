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
 * <p>Checklist for changing the contract template (any change to an XSL file, even whitespace or comments):
 * <ol>
 *   <li>Edit the XSL file(s) in src/main/resources/pdf.</li>
 *   <li>Append a new entry here, with the planned release date as {@code validFrom}.</li>
 *   <li>Run {@code ContractTemplateVersionEnumTest} and update {@code EXPECTED_CURRENT_VERSION} and
 *       {@code EXPECTED_TEMPLATE_HASH} (the "but was" value of the failing assertion).</li>
 *   <li>Commit the XSL change, the new entry and the test update together. No Flyway migration is needed.</li>
 * </ol>
 *
 * <p>After deployment, all contract revisions with an older version that are not fully signed are blocked from signing
 * ({@code CONTRACT_TEMPLATE_OUTDATED}) and their data requests must be withdrawn and resubmitted. Check the number of
 * affected contracts and inform consumers and providers before releasing.
 *
 * @CommentLastReviewed 2026-11-23
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
