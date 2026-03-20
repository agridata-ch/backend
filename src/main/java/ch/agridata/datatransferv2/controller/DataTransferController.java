package ch.agridata.datatransferv2.controller;

import static ch.agridata.common.openapi.ApiSubsetConstants.DATA_CONSUMER;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

import ch.agridata.common.openapi.ApiSubset;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.datatransferv2.dto.ProducerIdentifier;
import ch.agridata.datatransferv2.service.ChangeDetectionService;
import ch.agridata.datatransferv2.service.FlowEnum;
import ch.agridata.datatransferv2.service.FlowProvider;
import io.quarkus.security.Authenticated;
import io.quarkus.security.ForbiddenException;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Exposes the API endpoint for requesting data transfers. It validates producer identification by UID or BUR, enforces consent
 * requirements, and forwards requests to the transfer service.
 *
 * @CommentLastReviewed 2026-02-26
 */
@Path(DataTransferController.PATH)
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Data Transfer V2")
@RunOnVirtualThread
public class DataTransferController {
  public static final String PATH = "/api/data-transfer/v2";

  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final FlowProvider flowProvider;
  private final ChangeDetectionService changeDetectionService;

  @GET
  @ApiSubset({DATA_CONSUMER})
  @Path("/product/{productId}/data")
  @Operation(
      operationId = "dataTransferV2",
      description = "Retrieves data defined by productId. The needed query parameters are depending on the requested productId. "
          + "Please consult documentation to find the necessary parameters. "
          + "This endpoint simply forwards the payload from the source system to the consumer."
          + "Before any data is transferred, the producer must have accepted an active data request that includes the requested product."
  )
  @Produces(MediaType.WILDCARD)
  // @Authenticated suffices here: role enforcement is deferred to enforceConsumerRoleIfRequired(),
  // which skips the consumer-role check for post-validation flows that rely on provider-side authorization.
  @Authenticated
  public Response dataTransfer(
      @Parameter(
          name = "productId",
          description = "productId for which the data is requested",
          example = "085e4b72-964d-4bd5-a3c9-224d8c5585af"
      )
      @PathParam("productId") @Valid UUID productId,
      // This parameter is declared solely so that the Swagger UI renders the query-parameter
      // documentation correctly (via @Parameter + @Schema).
      // The actual query parameters are read generically from UriInfo below.
      @Parameter(
          name = "queryParams",
          description = "Additional query parameters. The needed query parameters are depending on the requested productId. "
              + "Please consult documentation to find the necessary parameters.",
          examples = @ExampleObject(
              name = "uid",
              value = "{\"uid\": \"CHE123456789\"}"
          ),
          schema = @Schema(
              type = SchemaType.OBJECT,
              implementation = Map.class
          )
      )
      @QueryParam("queryParams") List<String> queryParams,
      @Context UriInfo uriInfo
  ) {
    var flowWithConfiguration = flowProvider.getFlowByProduct(productId);
    enforceConsumerRoleIfRequired(flowWithConfiguration);
    Map<String, String> queryParameters = uriInfo.getQueryParameters(true).entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFirst()));
    return flowWithConfiguration.flow().run(flowWithConfiguration.productProviderConfiguration(), queryParameters);
  }

  private void enforceConsumerRoleIfRequired(FlowProvider.FlowWithProductProviderConfiguration flowWithConfiguration) {
    var flowsWithProviderSideAuthorization = List.of(
        FlowEnum.UID_BASED_POST_VALIDATION,
        FlowEnum.BUR_BASED_POST_VALIDATION,
        FlowEnum.UNBOUND_POST_VALIDATION);
    var flowCode = FlowEnum.valueOf(flowWithConfiguration.productProviderConfiguration().flowCode());
    if (flowsWithProviderSideAuthorization.contains(flowCode)) {
      return;
    }
    if (!agridataSecurityIdentity.isConsumer()) {
      throw new ForbiddenException();
    }
  }

  @GET
  @ApiSubset({DATA_CONSUMER})
  @Path("/product/{productId}/modified-producers")
  @Operation(
      operationId = "getModifiedProducers",
      description = "Returns producer IDs for which either a new consent was granted or data has changed at the upstream provider since "
          + "the given timestamp. Only producer IDs with a currently valid consent are included. "
          + "This endpoint requires the product to have change detection configured."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(CONSUMER_ROLE)
  public List<ProducerIdentifier> getModifiedProducers(
      @Parameter(
          name = "productId",
          description = "productId for which the change detection is requested",
          example = "085e4b72-964d-4bd5-a3c9-224d8c5585af"
      )
      @PathParam("productId") @Valid UUID productId,
      @Parameter(
          name = "since",
          description = "Only changes and new consents after this date are returned.",
          example = "2025-01-01"
      )
      @QueryParam("since") @NotNull @Valid LocalDate since
  ) {
    return changeDetectionService.getModifiedProducers(productId, since);
  }
}
