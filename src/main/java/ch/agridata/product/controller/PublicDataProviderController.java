package ch.agridata.product.controller;

import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.product.controller.PublicDataProviderController.PATH;

import ch.agridata.common.openapi.ApiSubset;
import ch.agridata.product.dto.DataProviderDto;
import ch.agridata.product.service.DataProviderService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Controller for public access to data providers.
 * Provides an endpoint to retrieve all available data providers, e.g. to populate filters on public data product listings.
 * This controller is accessible to all users.
 *
 * @CommentLastReviewed 2026-08-04
 */

@Path(PATH)
@RequiredArgsConstructor
@Tag(name = "Public Data Providers", description = "Endpoints for public data providers")
@RunOnVirtualThread
public class PublicDataProviderController {
  public static final String PATH = "/public/api/products/v2/data-providers";

  private final DataProviderService dataProviderService;

  @GET
  @ApiSubset({WEB_APP})
  @Operation(
      operationId = "getPublicDataProviders",
      description = "Retrieves the list of all available data providers. Publicly accessible."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  public List<DataProviderDto> getPublicDataProviders() {
    return dataProviderService.getDataProviders();
  }
}
