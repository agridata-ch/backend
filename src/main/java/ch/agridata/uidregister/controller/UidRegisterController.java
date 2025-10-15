package ch.agridata.uidregister.controller;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

import ch.agridata.uidregister.dto.UidRegisterOrganisationDto;
import ch.agridata.uidregister.service.UidRegisterService;
import ch.ech.xmlns.ech_0097._5.UidOrganisationIdCategorieType;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.math.BigInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Declares methods to retrieve organization details by a given UID or by the UID of the current authenticated user. It ensures consistent
 * contracts and error handling across services.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Path(UidRegisterController.PATH)
@Slf4j
@RequiredArgsConstructor
@Tag(
    name = "UID Register Search",
    description = "Provides access to organisation details retrieved from the official UID register.")
@RolesAllowed({CONSUMER_ROLE, ADMIN_ROLE})
@RunOnVirtualThread
public class UidRegisterController {

  public static final String PATH = "/api/uid-register/v1";
  private final UidRegisterService uidRegisterService;

  @GET
  @Path("/search/{uid}")
  @Operation(
      operationId = "getByUid",
      description = "Retrieves organisation details from the UID register using the specified UID. "
          + "Accessible to users with the admin role.")
  @RolesAllowed({ADMIN_ROLE})
  public UidRegisterOrganisationDto getByUid(
      @PathParam("uid")
      @Parameter(
          description = "UID of the organisation without CHE prefix",
          example = "101708094",
          required = true
      ) BigInteger uid) {
    return uidRegisterService.getByUid(UidOrganisationIdCategorieType.CHE, uid);
  }

  @GET
  @Path("/search")
  @Operation(
      operationId = "getByUidOfCurrentUser",
      description = "Retrieves organisation details from the UID register for the UID associated with the "
          + "currently authenticated consumer. Only accessible to users with the consumer role.")
  @RolesAllowed({CONSUMER_ROLE})
  public UidRegisterOrganisationDto getByUidOfCurrentUser() {
    return uidRegisterService.getByUidOfCurrentUser();
  }

}
