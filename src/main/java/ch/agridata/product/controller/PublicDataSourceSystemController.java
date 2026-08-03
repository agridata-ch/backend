package ch.agridata.product.controller;

import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.product.controller.PublicDataSourceSystemController.PATH;

import ch.agridata.common.openapi.ApiSubset;
import ch.agridata.product.dto.DataSourceSystemDto;
import ch.agridata.product.service.DataSourceSystemService;
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
 * Controller for public access to data source systems.
 * Provides an endpoint to retrieve all available data source systems, e.g. to populate filters on public data product listings.
 * This controller is accessible to all users.
 *
 * @CommentLastReviewed 2026-08-04
 */

@Path(PATH)
@RequiredArgsConstructor
@Tag(name = "Public Data Source Systems", description = "Endpoints for public data source systems")
@RunOnVirtualThread
public class PublicDataSourceSystemController {
  public static final String PATH = "/public/api/products/v2/data-source-systems";

  private final DataSourceSystemService dataSourceSystemService;

  @GET
  @ApiSubset({WEB_APP})
  @Operation(
      operationId = "getPublicDataSourceSystems",
      description = "Retrieves the list of all available data source systems. Publicly accessible."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  public List<DataSourceSystemDto> getPublicDataSourceSystems() {
    return dataSourceSystemService.getDataSourceSystems();
  }
}
