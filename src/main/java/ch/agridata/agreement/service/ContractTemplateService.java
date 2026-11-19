package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;

import ch.agridata.agreement.dto.ContractTemplateVersionDto;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

/**
 * Lists the versions of the contract template defined in {@link ContractTemplateVersionEnum}.
 *
 * @CommentLastReviewed 2026-11-18
 */
@ApplicationScoped
public class ContractTemplateService {

  /**
   * Returns all versions, newest first. {@code validTo} is the {@code validFrom} of the next version, null for the current one.
   */
  @RolesAllowed(ADMIN_ROLE)
  public List<ContractTemplateVersionDto> getAll() {
    var versions = ContractTemplateVersionEnum.values();
    var result = new ArrayList<ContractTemplateVersionDto>();

    for (var i = versions.length - 1; i >= 0; i--) {
      var validTo = i + 1 < versions.length ? versions[i + 1].getValidFrom() : null;
      result.add(new ContractTemplateVersionDto(versions[i].getVersion(), versions[i].getValidFrom(), validTo));
    }

    return result;
  }
}
