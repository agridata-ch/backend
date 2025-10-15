package ch.agridata.user.controller;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;
import static ch.agridata.user.controller.UserController.PATH;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.user.dto.BurDto;
import ch.agridata.user.dto.UidDto;
import ch.agridata.user.dto.UserInfoDto;
import ch.agridata.user.service.BurAuthorizationService;
import ch.agridata.user.service.UidAuthorizationService;
import ch.agridata.user.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Handles user-related API requests. It provides operations to fetch authorized UIDs and BURs for a given user or producer,
 * restricted by role.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Path(PATH)
@Slf4j
@RequiredArgsConstructor
@Tag(
    name = "Users",
    description = "Provides access to UIDs and BURs authorized for specific data producers, based on their ktIdP or UID.")
public class UserController {

  public static final String PATH = "/api/user/v1";

  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final UidAuthorizationService uidAuthorizationService;
  private final BurAuthorizationService burAuthorizationService;
  private final UserService userService;

  @GET
  @Path("/ktIdP/{ktIdP}/authorized-uids")
  @Operation(
      operationId = "getAuthorizedUidsByKtIdP",
      description = "Retrieves all UIDs authorized for the specified ktIdP. Only accessible to admin users.")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({ADMIN_ROLE})
  public List<UidDto> getAuthorizedUidsByKtIdP(@PathParam("ktIdP") String ktIdP) {
    return uidAuthorizationService.getAuthorizedUids(ktIdP);
  }

  @GET
  @Path("/authorized-uids")
  @Operation(
      operationId = "getAuthorizedUids",
      description = "Retrieves all UIDs authorized for the currently authenticated data producer.")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({PRODUCER_ROLE, SUPPORT_ROLE})
  public List<UidDto> getAuthorizedUids() {
    return uidAuthorizationService.getAuthorizedUids(agridataSecurityIdentity.getKtIdpOfUserOrImpersonatedUser());
  }

  @GET
  @Path("/uid/{uid}/authorized-burs")
  @Operation(
      operationId = "getAuthorizedBursByUid",
      description = "Retrieves all BURs authorized for the given UID. Only accessible to admin users.")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public List<BurDto> getAuthorizedBursByUid(@PathParam("uid") String uid) {
    return burAuthorizationService.getAuthorizedBurs(uid);
  }

  @GET
  @Path("/user-info")
  @Operation(
      operationId = "getUserInfo",
      description = "Retrieves user-info for the currently authenticated user.")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({PRODUCER_ROLE, CONSUMER_ROLE, ADMIN_ROLE, SUPPORT_ROLE, PROVIDER_ROLE})
  public UserInfoDto getUserInfo() {
    if (agridataSecurityIdentity.isSupport() && agridataSecurityIdentity.isImpersonating()) {
      return userService.getUserIdByKtIdP(agridataSecurityIdentity.getKtIdpOfUserOrImpersonatedUser());
    }
    return userService.updateUserData();
  }

  @GET
  @Path("/producers")
  @Operation(
      operationId = "getProducers",
      description = "Retrieves users mathing the given query parameters.")
  @Parameters({
      @Parameter(name = "resourceQueryDto",
          description = "Query parameters",
          schema = @Schema(implementation = ResourceQueryDto.class))
  })
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({SUPPORT_ROLE})
  public PageResponseDto<UserInfoDto> getProducers(
      @BeanParam @Valid ResourceQueryDto resourceQueryDto
  ) {
    return userService.getProducers(resourceQueryDto);
  }


}
