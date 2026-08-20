package ch.agridata.health.controller;

import static ch.agridata.common.openapi.ApiSubsetConstants.DATA_CONSUMER;
import static ch.agridata.common.openapi.ApiSubsetConstants.DATA_PROVIDER;
import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;
import static ch.agridata.health.controller.HealthController.PATH;

import ch.agridata.common.openapi.ApiSubset;
import ch.agridata.health.dto.HealthDto;
import ch.agridata.health.dto.HealthDto.HealthStatus;
import ch.agridata.health.service.HealthService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Health endpoint. Reports agridata.ch's readiness (which drives the HTTP status) plus an informational summary
 * of each connected data source system; a data provider being down never affects agridata.ch's status or the HTTP code.
 *
 * @CommentLastReviewed 2026-08-11
 */
@Path(PATH)
@RequiredArgsConstructor
@Tag(name = "Health", description = "Health status of agridata.ch and its connected data providers")
@RunOnVirtualThread
public class HealthController {
  public static final String PATH = "/api/health";

  private final HealthService healthService;

  @GET
  @ApiSubset({WEB_APP, DATA_CONSUMER, DATA_PROVIDER})
  @Operation(
      operationId = "getHealth",
      description = "Returns agridata.ch's readiness and an informational summary of the connected data providers. "
          + "A data provider being DOWN does not affect agridata.ch's status or the HTTP code."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({ADMIN_ROLE, SUPPORT_ROLE, CONSUMER_ROLE, PROVIDER_ROLE})
  public HealthDto getHealth() {
    var health = healthService.status();
    if (health.agridataStatus() != HealthStatus.UP) {
      throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(health).build());
    }
    return health;
  }
}
