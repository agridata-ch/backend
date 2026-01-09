package ch.agridata.workflowpoc.controller;

import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

import ch.agridata.workflowpoc.service.DummyFlow;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Dummy for workflow poc
 *
 * @CommentLastReviewed 2026-01-07
 */
@Path(DummyController.PATH)
@Slf4j
@RequiredArgsConstructor
@Tag(
    name = "Workflow PoC")
@RolesAllowed({CONSUMER_ROLE})
@RunOnVirtualThread
public class DummyController {
  public static final String PATH = "/api/workflow-poc";

  private final DummyFlow dummyFlow;

  @GET
  @Path("/product/{productId}")
  @Produces(MediaType.WILDCARD)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response dataTransfer(
      @Parameter(
          name = "productId",
          description = "productId for which the data is requested",
          example = "085e4b72-964d-4bd5-a3c9-224d8c5585af"
      )
      @PathParam("productId") @Valid UUID productId,
      @QueryParam("exampleQueryParam") String exampleQueryParam,
      @Context UriInfo uriInfo
  ) {
    Map<String, String> queryParameters = uriInfo.getQueryParameters(true).entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFirst()));

    return dummyFlow.run(productId, queryParameters);
  }
}
