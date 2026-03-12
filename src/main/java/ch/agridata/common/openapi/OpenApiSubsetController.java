package ch.agridata.common.openapi;

import io.smallrye.openapi.runtime.io.Format;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serves filtered OpenAPI documents containing only endpoints annotated with a specific {@code x-api-subset} value.
 *
 * @CommentLastReviewed 2026-02-16
 */

@Path("/q/openapi/subsets")
@Slf4j
@RequiredArgsConstructor
public class OpenApiSubsetController {

  private final OpenApiSubsetService openApiSubsetService;

  @GET
  @Path("/{subset}")
  @PermitAll
  public Response getSubset(
      @PathParam("subset") String subset,
      @QueryParam("format") String format
  ) {
    try {
      boolean json = "json".equalsIgnoreCase(format);
      Format outputFormat = json ? Format.JSON : Format.YAML;
      String mediaType = json ? MediaType.APPLICATION_JSON : "application/yaml";
      String document = openApiSubsetService.getFilteredDocument(subset, outputFormat);
      return Response.ok(document).type(mediaType).build();
    } catch (Exception e) {
      log.error("Failed to generate OpenAPI subset for '{}'", subset, e);
      return Response.serverError().entity("Failed to generate OpenAPI subset").build();
    }
  }
}
