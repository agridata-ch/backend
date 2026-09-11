package ch.agridata.agreement.controller;

import static ch.agridata.agreement.controller.ConsentRequestController.PATH;
import static ch.agridata.common.openapi.ApiSubsetConstants.MOBILE_APP;
import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;

import ch.agridata.agreement.dto.ConsentRequestCleanupResultDto;
import ch.agridata.agreement.dto.ConsentRequestCreatedDto;
import ch.agridata.agreement.dto.ConsentRequestProducerViewDto;
import ch.agridata.agreement.dto.ConsentRequestStateEnum;
import ch.agridata.agreement.dto.CreateConsentRequestDto;
import ch.agridata.agreement.service.AuditingService;
import ch.agridata.agreement.service.ConsentRequestCleanupRunner;
import ch.agridata.agreement.service.ConsentRequestCreationService;
import ch.agridata.agreement.service.ConsentRequestQueryService;
import ch.agridata.agreement.service.ConsentRequestStateService;
import ch.agridata.common.openapi.ApiSubset;
import ch.agridata.common.security.AgridataSecurityIdentity;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestResponse;

/**
 * Manages endpoints related to consent requests. It handles producer and consumer interactions with the consent lifecycle.
 *
 * @CommentLastReviewed 2026-09-07
 */

@Path(PATH)
@Slf4j
@RequiredArgsConstructor
@Tag(
    name = "Consent Requests",
    description = "Provides access to consent requests for data producers, consumers, and admins. "
        + "Data producers can retrieve and update consent requests assigned to them, "
        + "consumers can access consent requests linked to their data requests, "
        + "and admins have full access to all consent requests."
)
@RunOnVirtualThread
public class ConsentRequestController {

  public static final String PATH = "/api/agreement/v1/consent-requests";
  private final ConsentRequestQueryService consentRequestQueryService;
  private final ConsentRequestCreationService consentRequestCreationService;
  private final ConsentRequestStateService consentRequestStateService;
  private final ConsentRequestCleanupRunner consentRequestCleanupRunner;
  private final AuditingService auditingService;
  private final AgridataSecurityIdentity identity;

  /**
   * This method is deprecated.
   *
   * @deprecated Replaced by {@link ConsentRequestAggregationController#getConsentRequestAggregations(String)()}
   */
  @Deprecated(since = "1.15.0")
  @GET
  @ApiSubset({MOBILE_APP, WEB_APP})
  @Operation(
      operationId = "getConsentRequests",
      description = "Retrieves all consent requests assigned to the currently authenticated data producer. "
          + "Only accessible to users with the producer role."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({PRODUCER_ROLE, SUPPORT_ROLE})
  public List<ConsentRequestProducerViewDto> getConsentRequestsForCurrentDataProducer(
      @Parameter(
          name = "dataProducerUid",
          description = "Optional filter to retrieve consent requests for a specific producer UID. "
              + "If not provided, all requests for the currently authenticated producer are returned.",
          example = "CHE101000001"
      )
      @Pattern(
          regexp = "^(?:CHE|ZZZ)\\d{9}$",
          message = "Invalid UID format. Expected format is 'CHE' or 'ZZZ' followed by 9 digits."
      )
      @QueryParam("dataProducerUid") String dataProducerUid
  ) {
    return consentRequestQueryService.getConsentRequestsAsCurrentDataProducer(dataProducerUid);
  }

  /**
   * This method is deprecated.
   *
   * @deprecated Replaced by {@link ConsentRequestAggregationController#getConsentRequestAggregation(UUID, String)()}
   */
  @Deprecated(since = "1.15.0")
  @Path("/{id}")
  @ApiSubset({WEB_APP})
  @GET()
  @Operation(
      operationId = "getConsentRequest",
      description = "Retrieves consent request with {id} if the authenticated data producer is assigned to it. "
  )
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({PRODUCER_ROLE, SUPPORT_ROLE})
  public ConsentRequestProducerViewDto getConsentRequest(
      @PathParam("id")
      UUID id
  ) {
    return consentRequestQueryService.getConsentRequest(id);
  }

  @PUT
  @Path("/{id}/status")
  @ApiSubset({MOBILE_APP, WEB_APP})
  @Operation(
      operationId = "updateConsentRequestStatus",
      description = "Updates the status of a specific consent request. Only accessible to the "
          + "data producer assigned to the consent request."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(PRODUCER_ROLE)
  public void updateConsentRequestStateForCurrentDataProducer(
      @Parameter(description = "ID of the consent request", required = true)
      @PathParam("id") UUID id,
      @RequestBody(description = "New status of the consent request")
      ConsentRequestStateEnum newStatus
  ) {
    consentRequestStateService.updateConsentRequestStateAsCurrentDataProducer(id, newStatus);
  }

  @POST
  @ApiSubset({WEB_APP})
  @Operation(
      operationId = "createConsentRequests",
      description = "Creates consent requests for given uids, provided the user actually has access to those uids."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({PRODUCER_ROLE})
  @ResponseStatus(RestResponse.StatusCode.CREATED)
  public List<ConsentRequestCreatedDto> createConsentRequests(
      @NotNull @RequestBody List<@Valid @NotNull CreateConsentRequestDto> createConsentRequestDtos
  ) {
    return consentRequestCreationService.createConsentRequests(createConsentRequestDtos);
  }

  @POST
  @Path("/cleanup")
  @ApiSubset({WEB_APP})
  @Operation(
      operationId = "triggerConsentRequestCleanup",
      description = "Triggers the consent request cleanup that otherwise runs as a scheduled daily job, and waits for it "
          + "to finish. Only accessible to administrators. The run is guarded by the same cluster-wide advisory lock as "
          + "the scheduled job, so a conflict is reported if a cleanup is already running. By default, the same two-day "
          + "window as the scheduled job is used; an explicit window can be passed to catch up on days that were missed, "
          + "e.g. after an outage."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({ADMIN_ROLE})
  public ConsentRequestCleanupResultDto triggerConsentRequestCleanup(
      @Parameter(description = "First day of the window to clean up (inclusive). Defaults to two days ago.")
      @QueryParam("fromInclusive") @Valid LocalDate fromInclusive,
      @Parameter(description = "Last day of the window to clean up (inclusive). Defaults to yesterday.")
      @QueryParam("toInclusive") @Valid LocalDate toInclusive
  ) {
    // Read the identity and audit the trigger before the runner replaces it with the technical cleanup user.
    var triggeredBy = identity.getUserId();
    auditingService.logConsentRequestCleanupTriggered();
    log.info("consent request cleanup manually triggered by user {}.", triggeredBy);

    var result = consentRequestCleanupRunner.runCleanupIfNotRunning(fromInclusive, toInclusive)
        .orElseThrow(() -> new WebApplicationException(
            "A consent request cleanup is already running.", Response.Status.CONFLICT
        ));

    log.info(
        "consent request cleanup triggered by user {} terminated {} consent requests for {} - {} in {} ms.",
        triggeredBy,
        result.terminatedConsentRequestCount(),
        result.fromInclusive(),
        result.toInclusive(),
        result.durationMs()
    );

    return result;
  }
}
