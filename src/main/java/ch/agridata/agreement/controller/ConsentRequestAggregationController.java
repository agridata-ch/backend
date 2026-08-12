package ch.agridata.agreement.controller;

import static ch.agridata.agreement.controller.ConsentRequestAggregationController.PATH;
import static ch.agridata.common.openapi.ApiSubsetConstants.MOBILE_APP;
import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;

import ch.agridata.agreement.dto.ConsentRequestAggregationDto;
import ch.agridata.agreement.dto.ConsentRequestAggregationSummaryDto;
import ch.agridata.agreement.service.ConsentRequestAggregationService;
import ch.agridata.common.openapi.ApiSubset;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.GET;
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
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Manages endpoints for retrieving aggregated consent requests for data producers. Each aggregation groups consent requests by the data
 * request they belong to.
 *
 * @CommentLastReviewed 2026-02-04
 */

@Path(PATH)
@Slf4j
@RequiredArgsConstructor
@Tag(
    name = "Consent Request Aggregations",
    description = "Provides access to aggregated consent requests for data producers. "
        + "Each aggregation groups consent requests by the data request they belong to."
)
public class ConsentRequestAggregationController {
  public static final String PATH = "/api/agreement/v1/consent-request-aggregations";

  private final ConsentRequestAggregationService consentRequestAggregationService;

  @GET()
  @ApiSubset({MOBILE_APP, WEB_APP})
  @Operation(
      operationId = "getConsentRequestAggregations",
      description = "Retrieves summary aggregated consent requests for the current data producer, grouped by data request."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({PRODUCER_ROLE, SUPPORT_ROLE})
  public List<ConsentRequestAggregationSummaryDto> getConsentRequestAggregations(
      @Parameter(
          name = "dataProducerUid",
          description = "Filter to retrieve consent requests for a specific producer UID.",
          example = "CHE101000001",
          required = true
      )
      @NotBlank(message = "dataProducerUid is required")
      @Pattern(
          regexp = "^(?:CHE|ZZZ)\\d{9}$",
          message = "Invalid UID format. Expected format is 'CHE' or 'ZZZ' followed by 9 digits."
      )
      @QueryParam("dataProducerUid") String dataProducerUid
  ) {
    return consentRequestAggregationService.getConsentRequestAggregationsAsCurrentDataProducer(dataProducerUid);
  }

  @GET()
  @Path("/{id}")
  @ApiSubset({MOBILE_APP, WEB_APP})
  @Operation(
      operationId = "getConsentRequestAggregation",
      description = "Retrieves the full details of a single consent request aggregation for the current data producer."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({PRODUCER_ROLE, SUPPORT_ROLE})
  public ConsentRequestAggregationDto getConsentRequestAggregation(
      @PathParam("id") UUID id,
      @Parameter(
          name = "dataProducerUid",
          description = "Filter to retrieve the consent request aggregation for a specific producer UID.",
          example = "CHE101000001",
          required = true
      )
      @NotBlank(message = "dataProducerUid is required")
      @Pattern(
          regexp = "^(?:CHE|ZZZ)\\d{9}$",
          message = "Invalid UID format. Expected format is 'CHE' or 'ZZZ' followed by 9 digits."
      )
      @QueryParam("dataProducerUid") String dataProducerUid
  ) {
    return consentRequestAggregationService.getConsentRequestAggregationAsCurrentDataProducer(dataProducerUid, id);
  }

}
