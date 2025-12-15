package ch.agridata.datatransfer.controller;

import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

import ch.agridata.datatransfer.dto.DataTransferResponse;
import ch.agridata.datatransfer.service.DataTransferService;
import ch.agridata.datatransfer.service.DeltaService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Exposes the API endpoint for requesting data transfers. It validates producer identification by UID or BUR, enforces consent
 * requirements, and forwards requests to the transfer service.
 *
 * @CommentLastReviewed 2025-10-15
 */
@Path(DataTransferController.PATH)
@Slf4j
@RequiredArgsConstructor
@Tag(
    name = "Data Transfer",
    description = (
        "Relays data products that belong to a producer, identified by a specific UID or local BUR local unit Id. "
            + "Before any data is transferred, the producer must have accepted an active data request that includes the requested product. "
            + "This endpoint simply forwards the payload from the source system to the consumer."
    ))
@RolesAllowed({CONSUMER_ROLE})
@RunOnVirtualThread
public class DataTransferController {
  public static final String PATH = "/api/data-transfer/v1";
  private final DataTransferService dataTransferService;
  private final DeltaService deltaService;

  @GET
  @Path("/product/{productId}/data")
  @Operation(
      operationId = "dataTransfer",
      description = "Retrieves data defined by productId. The needed query parameters are depending on the requested productId. "
          + "Please consult documentation to find the necessary parameters"
  )
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public DataTransferResponse dataTransfer(
      @Parameter(
          name = "productId",
          description = "productId for which the data is requested",
          example = "085e4b72-964d-4bd5-a3c9-224d8c5585af"
      )
      @PathParam("productId") @Valid UUID productId,

      @Parameter(
          name = "uid",
          description = "Optional filter to retrieve data of a producer identified by the uid",
          example = "CHE101000001"
      )
      @QueryParam("uid") @Valid String uid,

      @Parameter(
          name = "bur",
          description = "Optional filter to retrieve data of a producer identified by a id of a local bur unit"
      )
      @QueryParam("bur") @Valid String bur,

      @Parameter(
          name = "year",
          description = "year for which the data is requested",
          example = "2024"
      )
      @QueryParam("year") @Valid Integer year,
      @Context UriInfo uriInfo
  ) {
    Validate.isTrue((uid != null) ^ (bur != null), "Exactly one of uid or bur must be provided");
    Map<String, String> params = uriInfo.getQueryParameters(true).entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFirst()));

    if (uid != null) {
      return dataTransferService.transferDataByUid(productId, uid, params);
    }
    return dataTransferService.transferDataByBur(productId, bur, params);
  }

  @GET
  @Path("/product/{productId}/delta")
  @Operation(
      operationId = "getDeltaIds",
      description = "Returns a list of delta IDs (Producer UIDs) for the specified product, considering both data updates "
          + "and newly granted consents since the given timestamp. This enables consumers to identify only "
          + "those IDs for which a detail query is relevant"
  )
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public List<String> getDeltaIds(
      @Parameter(
          name = "productId",
          description = "productId for which the delta ids are requested",
          example = "085e4b72-964d-4bd5-a3c9-224d8c5585af"
      )
      @PathParam("productId") @Valid UUID productId,

      @Parameter(
          name = "since",
          description = "Only delta IDs with changes or newly granted consents after this timestamp are returned.",
          example = "2025-01-01T09:00:00"
      )
      @QueryParam("since") @Valid @NotNull LocalDateTime since
  ) {
    return deltaService.getDeltaIds(productId, since);
  }
}
