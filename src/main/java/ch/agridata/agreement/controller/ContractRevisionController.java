package ch.agridata.agreement.controller;

import static ch.agridata.agreement.controller.ContractRevisionController.PATH;
import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

import ch.agridata.agreement.dto.ContractRevisionDto;
import ch.agridata.agreement.service.ContractRevisionQueryService;
import ch.agridata.common.openapi.ApiSubset;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Provides an endpoint for retrieving contract revisions for consumers.
 *
 * @CommentLastReviewed 2026-03-16
 */

@Slf4j
@Path(PATH)
@RequiredArgsConstructor
@Tag(
    name = "Contract Revisions",
    description = "Provides access to contract revisions for consumers"
)
@RolesAllowed({CONSUMER_ROLE})
@RunOnVirtualThread
public class ContractRevisionController {
  public static final String PATH = "/api/agreement/v1/contract-revisions";

  private final ContractRevisionQueryService contractRevisionQueryService;

  @GET
  @ApiSubset({WEB_APP})
  @Path("/{id}")
  @Operation(
      operationId = "getContractRevision",
      description = "Retrieves a specific contract revisionby its ID. Accessible by the consumer"
          + "that owns the associated datarequest."
  )
  @Produces(MediaType.APPLICATION_JSON)
  public ContractRevisionDto getContractRevision(@PathParam("id") UUID contractRevisionId) {
    return contractRevisionQueryService.getContractRevisionOfCurrentConsumer(contractRevisionId);
  }
}
