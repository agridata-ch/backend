package ch.agridata.agreement.controller;

import static ch.agridata.agreement.controller.ConsentRequestController.PATH;
import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;

import ch.agridata.agreement.dto.ConsentRequestCreatedDto;
import ch.agridata.agreement.dto.ConsentRequestProducerViewDto;
import ch.agridata.agreement.dto.ConsentRequestStateEnum;
import ch.agridata.agreement.dto.CreateConsentRequestDto;
import ch.agridata.agreement.service.ConsentRequestMutationService;
import ch.agridata.agreement.service.ConsentRequestQueryService;
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
import jakarta.ws.rs.core.MediaType;
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
 * @CommentLastReviewed 2025-10-23
 */

@Path(PATH)
@Slf4j
@RequiredArgsConstructor
@Tag(
    name = "Consent Requests",
    description = "Provides access to consent requests for data producers, consumers, and admins. "
        + "Data producers can retrieve and update consent requests assigned to them, "
        + "consumers can access consent requests linked to their data requests, "
        + "and admins have full access to all consent requests.")
@RolesAllowed({PRODUCER_ROLE, CONSUMER_ROLE, ADMIN_ROLE, SUPPORT_ROLE})
@RunOnVirtualThread
public class ConsentRequestController {

  public static final String PATH = "/api/agreement/v1/consent-requests";
  private final ConsentRequestQueryService consentRequestQueryService;
  private final ConsentRequestMutationService consentRequestMutationService;

  @GET
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
          example = "CHE123456789"
      )
      @Pattern(
          regexp = "^CHE[A-Za-z0-9*]{1,9}$", // Pattern must also support masked UIDs like 'CHE***123' to allow partially obscured data
          message = "Invalid UID format. Expected format is 'CHE' followed by 9 digits."
      )
      @QueryParam("dataProducerUid") String dataProducerUid
  ) {
    return consentRequestQueryService.getConsentRequestsAsCurrentDataProducer(dataProducerUid);
  }

  @PUT
  @Path("/{id}/status")
  @Operation(
      operationId = "updateConsentRequestStatus",
      description = "Updates the status of a specific consent request. Only accessible to the "
          + "data producer assigned to the consent request.")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(PRODUCER_ROLE)
  public void updateConsentRequestStateForCurrentDataProducer(
      @Parameter(description = "ID of the consent request", required = true)
      @PathParam("id") UUID id,
      @RequestBody(description = "New status of the consent request")
      ConsentRequestStateEnum newStatus
  ) {
    consentRequestMutationService.updateConsentRequestStateAsCurrentDataProducer(id, newStatus);
  }

  @POST
  @Operation(
      operationId = "createConsentRequests",
      description = "Creates consent requests for given uids, provided the user actually has access to those uids."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({PRODUCER_ROLE})
  @ResponseStatus(RestResponse.StatusCode.CREATED)
  public List<ConsentRequestCreatedDto> createConsentRequests(
      @Valid @NotNull @RequestBody List<CreateConsentRequestDto> createConsentRequestDtos) {
    return consentRequestMutationService.createConsentRequestForDataRequest(createConsentRequestDtos);
  }

}
