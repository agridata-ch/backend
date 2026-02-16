package ch.agridata.datatransferv2.controller;

import static ch.agridata.common.openapi.ApiSubsetConstants.DATA_CONSUMER;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

import ch.agridata.common.openapi.ApiSubset;
import ch.agridata.datatransferv2.service.FlowProvider;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Exposes the API endpoint for requesting data transfers. It validates producer identification by UID or BUR, enforces consent
 * requirements, and forwards requests to the transfer service.
 *
 * @CommentLastReviewed 2026-02-04
 */
@Path(DataTransferController.PATH)
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Data Transfer V2")
@RolesAllowed({CONSUMER_ROLE})
@RunOnVirtualThread
public class DataTransferController {
  public static final String PATH = "/api/data-transfer/v2";

  private final FlowProvider flowProvider;

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
  public Response dataTransfer(
      @Parameter(
          name = "productId",
          description = "productId for which the data is requested",
          example = "085e4b72-964d-4bd5-a3c9-224d8c5585af"
      )
      @PathParam("productId") @Valid UUID productId,
      @Context UriInfo uriInfo
  ) {
    var flow = flowProvider.getFlowByProduct(productId);
    Map<String, String> requestParameters = uriInfo.getQueryParameters(true).entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFirst()));
    return flow.run(productId, requestParameters);
  }
}
