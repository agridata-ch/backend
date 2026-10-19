package ch.agridata.user.controller;

import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.user.controller.AgbRevisionAdminController.PATH;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.openapi.ApiSubset;
import ch.agridata.user.dto.AgbRevisionOverviewDto;
import ch.agridata.user.service.AgbRevisionService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Controller for the AgbRevisionEntity. It is used by admins to list all agb revisions.
 *
 * @CommentLastReviewed 2026-10-01
 */

@Path(PATH)
@RequiredArgsConstructor
@Tag(
    name = "Agb Revision Admin",
    description = "Provides admin access to Agb revisions."
)
@RunOnVirtualThread
public class AgbRevisionAdminController {

  public static final String PATH = "/api/user/v1/admin/agb-revisions";
  private final AgbRevisionService agbRevisionService;

  @GET
  @Path("/")
  @ApiSubset({WEB_APP})
  @Operation(
      operationId = "getAllAgbRevisions",
      description = "Returns all Agb revisions. Supports pagination and sorting on 'version', 'validFrom', "
          + "'validTo' and 'enforceConsentFrom' (default: -validFrom)."
  )
  @Parameter(name = "resourceQueryDto",
      description = "Query parameters",
      schema = @Schema(implementation = ResourceQueryDto.class))
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({ADMIN_ROLE})
  public PageResponseDto<AgbRevisionOverviewDto> getAllAgbRevisions(
      @BeanParam @Valid ResourceQueryDto resourceQueryDto
  ) {
    return agbRevisionService.getAllRevisions(resourceQueryDto);
  }
}
