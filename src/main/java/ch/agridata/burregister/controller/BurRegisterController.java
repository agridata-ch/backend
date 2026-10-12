package ch.agridata.burregister.controller;

import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

import ch.agridata.burregister.dto.BurRegisterOrganisationDto;
import ch.agridata.burregister.service.BurRegisterService;
import ch.agridata.common.openapi.ApiSubset;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Exposes organisation details from the BUR (Business and Enterprise Register) by a given UID (admin) or by the UID of the current
 * authenticated consumer.
 *
 * @CommentLastReviewed 2026-10-09
 */
@Path(BurRegisterController.PATH)
@RequiredArgsConstructor
@Tag(
    name = "BUR Register Search",
    description = "Provides access to organisation details retrieved from the BUR (Business and Enterprise Register).")
@RunOnVirtualThread
public class BurRegisterController {

  public static final String PATH = "/api/bur-register/v1";
  private final BurRegisterService burRegisterService;

  @GET
  @ApiSubset({WEB_APP})
  @Path("/search/{uid}")
  @Operation(
      operationId = "getBurRegisterByUid",
      description = "Retrieves organisation details from the BUR using the specified UID. "
          + "Accessible to users with the admin role.")
  @RolesAllowed({ADMIN_ROLE})
  public BurRegisterOrganisationDto getByUid(
      @PathParam("uid")
      @Parameter(
          description = "UID of the organisation including the CHE prefix",
          example = "CHE113614519",
          required = true
      )
      @Pattern(
          regexp = BurRegisterService.UID_REGEX,
          message = "Invalid UID format. Expected format is 'CHE' followed by 9 digits."
      ) String uid) {
    return burRegisterService.getByUid(uid);
  }

  @GET
  @ApiSubset({WEB_APP})
  @Path("/search")
  @Operation(
      operationId = "getBurRegisterByUidOfCurrentUser",
      description = "Retrieves organisation details from the BUR for the UID associated with the "
          + "currently authenticated consumer. Only accessible to users with the consumer role.")
  @RolesAllowed({CONSUMER_ROLE})
  public BurRegisterOrganisationDto getByUidOfCurrentUser() {
    return burRegisterService.getByUidOfCurrentUser();
  }
}
