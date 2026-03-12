package ch.agridata.agreement.controller;

import static ch.agridata.agreement.controller.ConsentRequestAggregationController.PATH;
import static ch.agridata.common.openapi.ApiSubsetConstants.MOBILE_APP;
import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;

import ch.agridata.agreement.dto.ConsentRequestAggregationProducerView;
import ch.agridata.agreement.service.ConsentRequestAggregationQueryService;
import ch.agridata.common.openapi.ApiSubset;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
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

  private final ConsentRequestAggregationQueryService consentRequestAggregationQueryService;

  @GET()
  @ApiSubset({MOBILE_APP, WEB_APP})
  @Operation(
      operationId = "getConsentRequestAggregations",
      description = "Retrieves aggregated consent requests for the current data producer, grouped by data request."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({PRODUCER_ROLE, SUPPORT_ROLE})
  public List<ConsentRequestAggregationProducerView> getConsentRequestAggregations(
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
    return consentRequestAggregationQueryService.getConsentRequestAggregationsAsCurrentDataProducer(dataProducerUid);
  }

}
