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
import ch.agridata.user.dto.UserPreferencesDto;
import ch.agridata.user.service.BurAuthorizationService;
import ch.agridata.user.service.UidAuthorizationService;
import ch.agridata.user.service.UserService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestResponse;

/**
 * Handles user-related API requests. It provides operations to fetch authorized UIDs and BURs for a given user or producer,
 * restricted by role.
 *
 * @CommentLastReviewed 2025-10-15
 */

@Path(PATH)
@Slf4j
@RequiredArgsConstructor
@Tag(
    name = "Users",
    description = "Provides access to UIDs and BURs authorized for specific data producers, based on their ktIdP or UID.")
@RunOnVirtualThread
public class UserController {

  public static final String PATH = "/api/user/v1";

  private final AgridataSecurityIdentity identity;
  private final UidAuthorizationService uidAuthorizationService;
  private final BurAuthorizationService burAuthorizationService;
  private final UserService userService;

  @GET
  @Path("/authorized-uids")
  @Operation(
      operationId = "getAuthorizedUids",
      description = "Retrieves all UIDs authorized for the currently authenticated data producer.")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({PRODUCER_ROLE, SUPPORT_ROLE, ADMIN_ROLE})
  public List<UidDto> getAuthorizedUids(
      @Parameter(
          description = "The kt-id-p identifier of the producer (only relevant for admin users)",
          example = "FLXXA0001"
      )
      @QueryParam("kt-id-p") String ktIdP,
      @Parameter(
          description = "The agateLoginId of the producer (only relevant for admin users)",
          example = "1234567"
      )
      @QueryParam("agate-login-id") String agateLoginId) {
    if (identity.isAdmin()) {
      return uidAuthorizationService.getAuthorizedUids(ktIdP, agateLoginId);
    }
    return uidAuthorizationService.getAuthorizedUids(
        identity.getKtIdpOrImpersonatedKtIdP(),
        identity.getAgateLoginIdOrImpersonatedAgateLoginId());
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
    if (identity.isSupport() && identity.isImpersonating()) {
      return userService.getUserInfo(identity.getAgateLoginIdOrImpersonatedAgateLoginId());
    }
    return userService.updateUserData();
  }

  @PUT
  @Path("/preferences")
  @Operation(
      operationId = "updateUserPreferences",
      description = "updates preferences for the currently authenticated user.")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({PRODUCER_ROLE, CONSUMER_ROLE, ADMIN_ROLE, SUPPORT_ROLE, PROVIDER_ROLE})
  @ResponseStatus(RestResponse.StatusCode.CREATED)
  public void updateUserPreferences(@Valid @RequestBody UserPreferencesDto userPreferences) {
    if (!identity.isImpersonating()) {
      userService.updateUserPreferences(userPreferences);
    }
  }

  @GET
  @Path("/producers")
  @Operation(
      operationId = "getProducers",
      description = "Retrieves users mathing the given query parameters.")
  @Parameter(name = "resourceQueryDto",
      description = "Query parameters",
      schema = @Schema(implementation = ResourceQueryDto.class))
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({SUPPORT_ROLE})
  public PageResponseDto<UserInfoDto> getProducers(
      @BeanParam @Valid ResourceQueryDto resourceQueryDto
  ) {
    return userService.getProducers(resourceQueryDto);
  }

}
