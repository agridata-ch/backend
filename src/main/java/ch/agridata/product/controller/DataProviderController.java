package ch.agridata.product.controller;


import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;
import static ch.agridata.product.controller.DataProviderController.PATH;

import ch.agridata.common.openapi.ApiSubset;
import ch.agridata.common.security.actingrole.ActingRoleHolder;
import ch.agridata.common.security.actingrole.EnableActingRoleHolder;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataProviderDto;
import ch.agridata.product.dto.DataSourceSystemDto;
import ch.agridata.product.dto.RestClientDto;
import ch.agridata.product.service.DataProviderService;
import ch.agridata.product.service.DataSourceSystemService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.ForbiddenException;
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
@RunOnVirtualThread
public class DataProviderController {

  public static final String PATH = "/api/products/v1/data-providers";

  private final DataProviderService dataProviderService;
  private final DataSourceSystemService dataSourceSystemService;
  private final ActingRoleHolder actingRoleHolder;

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
    return dataProviderService.getActiveDataProductsByProviderId(providerId);
  }

  @GET
  @ApiSubset({WEB_APP})
  @Path("/{providerId}/dataSourceSystems")
  @Operation(
      operationId = "getDataSourceSystemsByProviderId",
      description = "Retrieves all data source systems belonging to a specific data provider"
  )
  @RolesAllowed({PROVIDER_ROLE, ADMIN_ROLE})
  @EnableActingRoleHolder
  public List<DataSourceSystemDto> getDataSourceSystemsByProviderId(@PathParam("providerId") UUID providerId) {
    return switch (actingRoleHolder.getRole()) {
      case ADMIN -> dataSourceSystemService.getByProviderIdAsAdmin(providerId);
      case PROVIDER -> dataSourceSystemService.getByProviderIdAsProvider(providerId);
      default -> throw new ForbiddenException();
    };
  }

  @GET
  @ApiSubset({WEB_APP})
  @Path("/{providerId}/restClients")
  @Operation(
      operationId = "getRestClientsByProviderId",
      description = "Retrieves all rest clients that are assigned to a specific data provider"
  )
  @RolesAllowed({PROVIDER_ROLE, ADMIN_ROLE})
  @EnableActingRoleHolder
  public List<RestClientDto> getRestClientsByProviderId(@PathParam("providerId") UUID providerId) {
    return switch (actingRoleHolder.getRole()) {
      case ADMIN -> dataProviderService.getRestClientsByDataProviderIdAsAdmin(providerId);
      case PROVIDER -> dataProviderService.getRestClientsByDataProviderIdAsProvider(providerId);
      default -> throw new ForbiddenException();
    };
  }
}
