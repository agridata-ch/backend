package ch.agridata.agreement.controller;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

import ch.agridata.agreement.dto.ConsentRequestConsumerViewDto;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.DataRequestStateEnum;
import ch.agridata.agreement.dto.DataRequestUpdateDto;
import ch.agridata.agreement.service.ConsentRequestQueryService;
import ch.agridata.agreement.service.DataRequestLogoService;
import ch.agridata.agreement.service.DataRequestMutationService;
import ch.agridata.agreement.service.DataRequestQueryService;
import ch.agridata.agreement.service.DataRequestStateService;
import io.quarkus.security.identity.SecurityIdentity;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/**
 * Provides endpoints for creating, updating, and querying data requests. It orchestrates validation and delegates processing to services.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Slf4j
@Path(DataRequestController.PATH)
@RequiredArgsConstructor
@Tag(
    name = "Data Requests",
    description = "Provides access to data requests for consumers and admins. "
        + "Consumers can create, update, submit, and retrieve their own data requests, "
        + "while admins have full access to all data requests and their associated consent requests.")
@RolesAllowed({CONSUMER_ROLE, ADMIN_ROLE})
public class DataRequestController {

  public static final String PATH = "/api/agreement/v1/data-requests";

  private final DataRequestLogoService dataRequestLogoService;
  private final DataRequestQueryService dataRequestQueryService;
  private final DataRequestMutationService dataRequestMutationService;
  private final DataRequestStateService dataRequestStateService;
  private final ConsentRequestQueryService consentRequestQueryService;
  private final SecurityIdentity securityIdentity;

  @GET
  @Operation(
      operationId = "getDataRequests",
      description = "Retrieves a list of data requests. Admin users receive all data requests, "
          + "while consumers receive only the data requests they own."
  )
  @Produces(MediaType.APPLICATION_JSON)
  public List<DataRequestDto> getDataRequests() {
    if (isAdmin()) {
      return dataRequestQueryService.getAllDataRequests();
    }
    return dataRequestQueryService.getAllDataRequestsOfCurrentConsumer();
  }

  @GET
  @Path("/{id}")
  @Operation(
      operationId = "getDataRequest",
      description = "Retrieves a specific data request by its ID. Accessible to admin users "
          + "or the consumer who owns the data request. "
  )
  @Produces(MediaType.APPLICATION_JSON)
  public DataRequestDto getDataRequest(@PathParam("id") UUID requestId) {
    if (isAdmin()) {
      return dataRequestQueryService.getDataRequest(requestId);
    }
    return dataRequestQueryService.getDataRequestOfCurrentConsumer(requestId);
  }

  @GET
  @Path("/{id}/kt-id-p/{kt-id-p}/consent-requests")
  @Operation(
      operationId = "getConsentRequestsOfDataRequest",
      description = "Retrieves all consent requests associated with a specific data request. "
          + "Accessible to admin users or the consumer who owns the data request."
  )
  @Produces(MediaType.APPLICATION_JSON)
  public List<ConsentRequestConsumerViewDto> getConsentRequestsOfDataRequest(
      @Parameter(
          description = "The UUID of the data request",
          example = "3da3a459-d3c2-48af-b8d0-02bc95146468"
      )
      @PathParam("id") UUID dataRequestId,
      @Parameter(
          description = "The kt-id-p identifier of the producer",
          example = "***081"
      )
      @PathParam("kt-id-p") String ktIdP) {
    if (isAdmin()) {
      return consentRequestQueryService.getConsentRequestsOfDataRequestForKtIdP(dataRequestId, ktIdP);
    }
    return consentRequestQueryService.getConsentRequestsOfDataRequestOfCurrentConsumerForKtIdP(dataRequestId, ktIdP);
  }

  @POST
  @Operation(
      operationId = "createDataRequestDraft",
      description = "Creates a new data request in draft status. Only accessible to users with the consumer role."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @ResponseStatus(RestResponse.StatusCode.CREATED)
  @RolesAllowed(CONSUMER_ROLE)
  public DataRequestDto createDataRequestDraft(DataRequestUpdateDto dataRequestDto) {
    return dataRequestMutationService.createDataRequestDraft(dataRequestDto);
  }

  @PUT()
  @Path("/{id}")
  @Operation(
      operationId = "updateDataRequestDetails",
      description = "Updates the details of an existing data request. Only accessible to the consumer who owns the data request."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(CONSUMER_ROLE)
  public DataRequestDto updateDataRequestDetails(@PathParam("id") UUID requestId,
                                                 @Valid DataRequestUpdateDto dataRequestDto) {
    return dataRequestMutationService.updateDataRequestDetails(requestId, dataRequestDto);
  }

  @PUT
  @Path("/{id}/status")
  @Operation(
      operationId = "setDataRequestStatus",
      description = "sets status of data request. Only accessible to the consumer who owns the data request and to admins. "
          + "Only specific transitions are allowed."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({CONSUMER_ROLE, ADMIN_ROLE})
  public DataRequestDto setDataRequestStatus(@PathParam("id") UUID requestId,
                                             @Valid DataRequestStateEnum stateCode) {
    if (isAdmin()) {
      return dataRequestStateService.setStateAsAdmin(requestId, stateCode);
    }
    return dataRequestStateService.setStateAsConsumer(requestId, stateCode);

  }

  @PUT
  @Path("/{id}/logo")
  @Operation(
      operationId = "updateDataRequestLogo",
      description = "Updates the logo of a specific data request. Only accessible to the consumer who owns the data request."
  )
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(CONSUMER_ROLE)
  public void updateDataRequestLogo(@PathParam("id") UUID requestId,
                                    @RestForm("logo") FileUpload logo) {
    dataRequestLogoService.updateDataRequestLogo(requestId, logo);
  }

  private boolean isAdmin() {
    return securityIdentity.hasRole(ADMIN_ROLE);
  }


}
