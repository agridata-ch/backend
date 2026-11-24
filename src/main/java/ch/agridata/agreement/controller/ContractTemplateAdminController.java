package ch.agridata.agreement.controller;

import static ch.agridata.agreement.controller.ContractTemplateAdminController.PATH;
import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;

import ch.agridata.agreement.dto.ContractTemplateVersionDto;
import ch.agridata.agreement.service.ContractTemplateService;
import ch.agridata.common.openapi.ApiSubset;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Used by admins to access the contract template, e.g. to list its versions.
 *
 * @CommentLastReviewed 2026-11-18
 */
@Path(PATH)
@RequiredArgsConstructor
@Tag(
    name = "Contract Template Admin",
    description = "Provides admin access to the contract template."
)
@RunOnVirtualThread
public class ContractTemplateAdminController {

  public static final String PATH = "/api/agreement/v1/admin/contract-template-versions";

  private final ContractTemplateService contractTemplateService;

  @GET
  @ApiSubset({WEB_APP})
  @Operation(
      operationId = "getAllContractTemplateVersions",
      description = "Returns all contract template versions, newest first. 'validTo' is the start of the following version "
          + "and empty for the current version. Not paginated."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({ADMIN_ROLE})
  public List<ContractTemplateVersionDto> getAllContractTemplateVersions() {
    return contractTemplateService.getAll();
  }
}
