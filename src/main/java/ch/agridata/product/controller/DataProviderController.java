package ch.agridata.product.controller;


import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;
import static ch.agridata.product.controller.DataProviderController.PATH;

import ch.agridata.common.openapi.ApiSubset;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataProviderDto;
import ch.agridata.product.service.DataProviderService;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Handles API requests to list available data providers. It enforces role-based access control and returns product metadata for producers,
 * consumers, admins, and providers.
 *
 * @CommentLastReviewed 2026-02-06
 */
@Path(PATH)
@Slf4j
@RequiredArgsConstructor
@Tag(
    name = "Data Providers",
    description = "Enables retrieval of data providers and their available data products.")
@Authenticated
@RunOnVirtualThread
public class DataProviderController {

  public static final String PATH = "/api/products/v1/data-providers";

  private final DataProviderService dataProviderService;

  @GET
  @ApiSubset({WEB_APP})
  @Operation(
      operationId = "getDataProviders",
      description = "Retrieves the list of all available data providers. Accessible to users with the "
          + "producer, consumer, admin, or provider role.")
  @RolesAllowed({PRODUCER_ROLE, CONSUMER_ROLE, ADMIN_ROLE, PROVIDER_ROLE, SUPPORT_ROLE})
  public List<DataProviderDto> getDataProviders() {
    return dataProviderService.getDataProviders();
  }

  @GET
  @ApiSubset({WEB_APP})
  @Path("/{providerId}")
  @Operation(
      operationId = "getDataProviderById",
      description = "Retrieves a specific data provider.")
  @RolesAllowed({PRODUCER_ROLE, CONSUMER_ROLE, ADMIN_ROLE, PROVIDER_ROLE, SUPPORT_ROLE})
  public DataProviderDto getDataProviderById(@PathParam("providerId") UUID providerId) {
    return dataProviderService.getDataProviderById(providerId);
  }

  @GET
  @ApiSubset({WEB_APP})
  @Path("/{providerId}/products")
  @Operation(
      operationId = "getDataProductsByProviderId",
      description = "Retrieves all data products belonging to a specific data provider.")
  @RolesAllowed({PRODUCER_ROLE, CONSUMER_ROLE, ADMIN_ROLE, PROVIDER_ROLE, SUPPORT_ROLE})
  public List<DataProductDto> getDataProductsByProviderId(@PathParam("providerId") UUID providerId) {
    return dataProviderService.getDataProductsByProviderId(providerId);
  }

}
