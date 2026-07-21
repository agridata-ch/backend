package ch.agridata.user.controller;

import static ch.agridata.common.openapi.ApiSubsetConstants.MOBILE_APP;
import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.user.controller.AgbRevisionController.PATH;

import ch.agridata.common.openapi.ApiSubset;
import ch.agridata.user.dto.AgbRevisionDto;
import ch.agridata.user.service.AgbRevisionService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Controller for the AgbRevisionEntity. It is used to find the current revision of the agb text.
 *
 * @CommentLastReviewed 2026-07-16
 */

@Path(PATH)
@Slf4j
@RequiredArgsConstructor
@Tag(
    name = "Agb Revision",
    description = "Provides access to Agb revisions."
)
@RunOnVirtualThread
public class AgbRevisionController {

  public static final String PATH = "public/api/user/v1";
  private final AgbRevisionService agbRevisionService;

  @GET
  @Path("/current-agb-revision")
  @ApiSubset({WEB_APP, MOBILE_APP})
  @Operation(
      operationId = "getCurrentAgbRevision",
      description = "Returns the current Agb revision."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  public AgbRevisionDto getCurrentAgbRevision() {
    return agbRevisionService.getCurrentRevision();
  }
}
