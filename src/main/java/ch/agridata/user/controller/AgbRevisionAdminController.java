package ch.agridata.user.controller;

import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.user.controller.AgbRevisionAdminController.PATH;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.openapi.ApiSubset;
import ch.agridata.user.dto.AgbRevisionDto;
import ch.agridata.user.dto.AgbRevisionSummaryDto;
import ch.agridata.user.dto.CreateAgbRevisionDto;
import ch.agridata.user.service.AgbRevisionService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Controller for the AgbRevisionEntity. It is used by admins to list, view and publish agb revisions.
 *
 * @CommentLastReviewed 2026-10-27
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
      description = "Returns listing summaries of all Agb revisions. Supports pagination and sorting on 'version', 'validFrom', "
          + "'validTo' and 'enforceConsentFrom' (default: -validFrom)."
  )
  @Parameter(
      name = "resourceQueryDto",
      description = "Query parameters",
      schema = @Schema(implementation = ResourceQueryDto.class)
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({ADMIN_ROLE})
  public PageResponseDto<AgbRevisionSummaryDto> getAllAgbRevisions(
      @BeanParam @Valid ResourceQueryDto resourceQueryDto
  ) {
    return agbRevisionService.getAllRevisions(resourceQueryDto);
  }

  @GET
  @Path("/{id}")
  @ApiSubset({WEB_APP})
  @Operation(
      operationId = "getAgbRevision",
      description = "Returns a single Agb revision."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({ADMIN_ROLE})
  public AgbRevisionDto getAgbRevision(@PathParam("id") UUID id) {
    return agbRevisionService.getRevision(id);
  }

  @POST
  @Path("/")
  @ApiSubset({WEB_APP})
  @Operation(
      operationId = "createAgbRevision",
      description = "Publishes a new Agb revision. It is valid from now and ends the currently active revision. If "
          + "'enforceConsentFrom' is set, it is applied to all data providers and data consumers with a later or no date. "
          + "Fails if the version already exists."
  )
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({ADMIN_ROLE})
  public Response createAgbRevision(@Valid @NotNull CreateAgbRevisionDto createDto) {
    var created = agbRevisionService.createRevision(createDto);
    return Response.created(UriBuilder.fromPath(PATH).path(created.id().toString()).build())
        .entity(created)
        .build();
  }
}
