package ch.agridata.agreement.controller;

import static ch.agridata.common.openapi.ApiSubsetConstants.DATA_CONSUMER;
import static ch.agridata.common.openapi.ApiSubsetConstants.DATA_PROVIDER;
import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;

import ch.agridata.agreement.dto.ConsentRequestConsumerViewDto;
import ch.agridata.agreement.dto.ConsentRequestConsumerViewV2Dto;
import ch.agridata.agreement.dto.ConsentRequestFundamentalViewDto;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.DataRequestStateEnum;
import ch.agridata.agreement.dto.DataRequestUpdateDto;
import ch.agridata.agreement.dto.DataRequestValidRedirectUriRegexUpdateDto;
import ch.agridata.agreement.service.ConsentRequestQueryService;
import ch.agridata.agreement.service.DataRequestLogoService;
import ch.agridata.agreement.service.DataRequestMutationService;
import ch.agridata.agreement.service.DataRequestQueryService;
import ch.agridata.agreement.service.DataRequestStateService;
import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.openapi.ApiSubset;
import ch.agridata.common.security.AgridataSecurityIdentity;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
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
@Path("")
@RequiredArgsConstructor
@Tag(
    name = "Data Requests",
    description = "Provides access to data requests for consumers and admins. "
        + "Consumers can create, update, submit, and retrieve their own data requests, "
        + "while admins have full access to all data requests and their associated consent requests.")
@RunOnVirtualThread
public class DataRequestController {

  public static final String PATH_V1 = "/api/agreement/v1/data-requests";
  public static final String PATH_V2 = "/api/agreement/v2/data-requests";

  private final DataRequestLogoService dataRequestLogoService;
  private final DataRequestQueryService dataRequestQueryService;
  private final DataRequestMutationService dataRequestMutationService;
  private final DataRequestStateService dataRequestStateService;
  private final ConsentRequestQueryService consentRequestQueryService;
  private final AgridataSecurityIdentity identity;

  @GET
  @ApiSubset({WEB_APP, DATA_CONSUMER, DATA_PROVIDER})
  @Path(PATH_V1)
  @Operation(
      operationId = "getDataRequests",
      description = "Retrieves a list of data requests. Admin users receive all data requests, "
          + "while consumers receive only the data requests they own."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({CONSUMER_ROLE, ADMIN_ROLE, PROVIDER_ROLE})
  public List<DataRequestDto> getDataRequests() {
    if (identity.isAdmin()) {
      return dataRequestQueryService.getAllNonDraftDataRequests();
    } else if (identity.isProvider()) {
      return dataRequestQueryService.getActiveDataRequestsForCurrentProvider();
    }
    return dataRequestQueryService.getAllDataRequestsOfCurrentConsumer();
  }

  @GET
  @ApiSubset({WEB_APP})
  @Path(PATH_V1 + "/{id}")
  @Operation(
      operationId = "getDataRequest",
      description = "Retrieves a specific data request by its ID. Accessible to admin users "
          + "or the consumer who owns the data request. "
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({CONSUMER_ROLE, ADMIN_ROLE, PROVIDER_ROLE})
  public DataRequestDto getDataRequest(@PathParam("id") UUID requestId) {
    if (identity.isAdmin()) {
      return dataRequestQueryService.getNonDraftDataRequest(requestId);
    } else if (identity.isProvider()) {
      return dataRequestQueryService.getActiveDataRequestForCurrentProvider(requestId);
    }
    return dataRequestQueryService.getDataRequestOfCurrentConsumer(requestId);
  }

  @GET
  @ApiSubset({DATA_PROVIDER})
  @Path(PATH_V1 + "/{id}/consent-requests")
  @Operation(
      operationId = "getConsentRequestsOfDataRequest",
      description = "Retrieves the consent requests of a specific data request. Accessible to the provider who owns the data request."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({PROVIDER_ROLE})
  public PageResponseDto<ConsentRequestFundamentalViewDto> getConsentRequestsOfDataRequest(
      @PathParam("id") UUID dataRequestId,
      @Parameter(
          name = "lastModifiedFrom",
          description = "Only consent requests that were modified after this timestamp are returned.",
          example = "2025-01-01T09:00:00"
      )
      @QueryParam("lastModifiedFrom") @DefaultValue("1970-01-01T00:00:00") @Valid LocalDateTime lastModifiedFrom,
      @QueryParam("page") @DefaultValue("0") @Min(0) int page,
      @QueryParam("size") @DefaultValue("100") @Min(1) @Max(1000) int size) {

    var resourceQueryDto = ResourceQueryDto.builder().sortParams(List.of("-modifiedAt")).page(page).size(size).build();
    return consentRequestQueryService.getConsentRequestsOfDataRequestAndCurrentProviderAndLastModifiedFrom(
        resourceQueryDto,
        dataRequestId,
        lastModifiedFrom);
  }

  /**
   * This method is deprecated, because it does not return the name of the UIDs
   *
   * @deprecated Replaced by {@link #getConsentRequestsOfDataRequestAndKtIdPv2(UUID, String)}
   */
  @Deprecated(since = "1.5.0")
  @GET
  @ApiSubset({DATA_CONSUMER})
  @Path(PATH_V1 + "/{id}/kt-id-p/{kt-id-p}/consent-requests")
  @Operation(
      operationId = "getConsentRequestsOfDataRequestAndKtIdP",
      description =
          "<strong>This endpoint is deprecated, because it does not return the name of the UIDs. Please use "
              + "[/v2/data-requests/{id}/kt-id-p/{kt-id-p}/consent-requests](#/Data%20Requests/getConsentRequestsOfDataRequestAndKtIdPv2) "
              + "instead.</strong><br><br>"
              + "Retrieves all consent requests associated with a specific data request and kt-id-p. "
              + "Accessible to the consumer who owns the data request."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(CONSUMER_ROLE)
  public List<ConsentRequestConsumerViewDto> getConsentRequestsOfDataRequestAndKtIdP(
      @Parameter(
          description = "The UUID of the data request",
          example = "3da3a459-d3c2-48af-b8d0-02bc95146468"
      )
      @PathParam("id") UUID dataRequestId,
      @Parameter(
          description = "The kt-id-p identifier of the producer",
          example = "FLXXA0001"
      )
      @PathParam("kt-id-p") String ktIdP
  ) {
    return consentRequestQueryService.getConsentRequestsOfDataRequestOfCurrentConsumerForKtIdP(dataRequestId, ktIdP);
  }

  @GET
  @ApiSubset({DATA_CONSUMER})
  @Path(PATH_V2 + "/{id}/kt-id-p/{kt-id-p}/consent-requests")
  @Operation(
      operationId = "getConsentRequestsOfDataRequestAndKtIdPv2",
      description = "Retrieves all consent requests associated with a specific data request and kt-id-p. "
          + "Accessible to the consumer who owns the data request and for admin users."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({CONSUMER_ROLE, ADMIN_ROLE})
  public List<ConsentRequestConsumerViewV2Dto> getConsentRequestsOfDataRequestAndKtIdPv2(
      @Parameter(
          description = "The UUID of the data request",
          example = "3da3a459-d3c2-48af-b8d0-02bc95146468"
      )
      @PathParam("id") UUID dataRequestId,
      @Parameter(
          description = "The kt-id-p identifier of the producer",
          example = "FLXXA0001"
      )
      @PathParam("kt-id-p") String ktIdP
  ) {
    if (identity.isAdmin()) {
      return consentRequestQueryService.getConsentRequestsOfDataRequestAndProducer(dataRequestId, ktIdP, null);
    }
    return consentRequestQueryService.getConsentRequestsOfDataRequestOfCurrentConsumerAndProducer(dataRequestId, ktIdP, null);
  }

  @POST
  @ApiSubset({WEB_APP})
  @Path(PATH_V1)
  @Operation(
      operationId = "createDataRequestDraft",
      description = "Creates a new data request in draft status. Only accessible to users with the consumer role. "
          + "Disallows creating more than 10 draft requests per consumer."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @ResponseStatus(RestResponse.StatusCode.CREATED)
  @RolesAllowed(CONSUMER_ROLE)
  public DataRequestDto createDataRequestDraft(@Valid DataRequestUpdateDto dataRequestDto) {
    return dataRequestMutationService.createDataRequestDraft(dataRequestDto);
  }

  @PUT()
  @ApiSubset({WEB_APP})
  @Path(PATH_V1 + "/{id}")
  @Operation(
      operationId = "updateDataRequestDetails",
      description = "Updates the details of an existing data request. Only accessible to the consumer who owns the data request."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(CONSUMER_ROLE)
  public DataRequestDto updateDataRequestDetails(
      @PathParam("id") UUID requestId,
      @Valid DataRequestUpdateDto dataRequestDto
  ) {
    return dataRequestMutationService.updateDataRequestDetails(requestId, dataRequestDto);
  }

  @DELETE
  @ApiSubset({WEB_APP})
  @Path(PATH_V1 + "/{id}")
  @Operation(
      operationId = "deleteDataRequest",
      description = "Deletes a data request"
  )
  @RolesAllowed(CONSUMER_ROLE)
  public Response deleteDataRequest(@PathParam("id") UUID requestId) {
    dataRequestMutationService.deleteDataRequest(requestId);
    return Response.noContent().build();
  }

  @PUT
  @ApiSubset({WEB_APP})
  @Path(PATH_V1 + "/{id}/status")
  @Operation(
      operationId = "setDataRequestStatus",
      description = "sets status of data request. Only accessible to the consumer who owns the data request and to admins. "
          + "Only specific transitions are allowed."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({CONSUMER_ROLE, ADMIN_ROLE})
  public DataRequestDto setDataRequestStatus(
      @PathParam("id") UUID requestId,
      @Valid DataRequestStateEnum stateCode
  ) {
    if (identity.isAdmin()) {
      return dataRequestStateService.setStateAsAdmin(requestId, stateCode);
    }
    return dataRequestStateService.setStateAsConsumer(requestId, stateCode);

  }

  @PUT
  @ApiSubset({WEB_APP})
  @Path(PATH_V1 + "/{id}/valid-redirect-uri-regex")
  @Operation(
      operationId = "updateDataRequestValidRedirectUriRegex",
      description = "Updates the valid redirect URI regex of a specific data request. Only accessible to admins."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public DataRequestDto updateDataRequestValidRedirectUriRegex(
      @PathParam("id") UUID requestId,
      @Valid DataRequestValidRedirectUriRegexUpdateDto dto
  ) {
    return dataRequestMutationService.updateValidRedirectUriRegex(requestId, dto);
  }

  @PUT
  @ApiSubset({WEB_APP})
  @Path(PATH_V1 + "/{id}/logo")
  @Operation(
      operationId = "updateDataRequestLogo",
      description = "Updates the logo of a specific data request. Only accessible to the consumer who owns the data request."
  )
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(CONSUMER_ROLE)
  public void updateDataRequestLogo(
      @PathParam("id") UUID requestId,
      @RestForm("logo") FileUpload logo
  ) {
    dataRequestLogoService.updateDataRequestLogo(requestId, logo);
  }

}
