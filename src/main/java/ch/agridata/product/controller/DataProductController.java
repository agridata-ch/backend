package ch.agridata.product.controller;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;
import static ch.agridata.product.controller.DataProductController.PATH;

import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.service.DataProductService;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Handles API requests to list available data products. It enforces role-based access control and returns product metadata for producers,
 * consumers, admins, and providers.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Path(PATH)
@Slf4j
@RequiredArgsConstructor
@Tag(
    name = "Data Products",
    description = "Enables retrieval of available data products that can be requested "
        + "and used by Users in the data sharing process.")
@Authenticated
@RunOnVirtualThread
public class DataProductController {
  public static final String PATH = "/api/products/v1";
  private final DataProductService dataProductService;

  @GET
  @Operation(
      operationId = "getDataProducts",
      description = "Retrieves the list of all available data products. Accessible to users with the "
          + "producer, consumer, admin, or provider role.")
  @RolesAllowed({PRODUCER_ROLE, CONSUMER_ROLE, ADMIN_ROLE, PROVIDER_ROLE, SUPPORT_ROLE})
  public List<DataProductDto> getDataProducts() {
    return dataProductService.getDataProducts();
  }
}
