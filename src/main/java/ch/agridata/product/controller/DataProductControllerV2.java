package ch.agridata.product.controller;

import static ch.agridata.common.openapi.ApiSubsetConstants.MOBILE_APP;
import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;
import static ch.agridata.product.controller.DataProductControllerV2.PATH;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.dto.SupportedLanguage;
import ch.agridata.common.openapi.ApiSubset;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.common.security.actingrole.ActingRoleHolder;
import ch.agridata.common.security.actingrole.EnableActingRoleHolder;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.service.DataProductService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Handles API requests that returns paged lists of available data products. It enforces role-based
 * access control and returns product metadata for admins and providers.
 *
 * @CommentLastReviewed 2026-05-18
 */

@Path(PATH)
@Slf4j
@RequiredArgsConstructor
@Tag(
    name = "Data Products",
    description = "Enables retrieval of available data products that can be requested "
        + "and used by Users in the data sharing process.")
@RunOnVirtualThread
public class DataProductControllerV2 {
  public static final String PATH = "/api/products/v2";
  private final DataProductService dataProductService;
  private final AgridataSecurityIdentity identity;
  private final ActingRoleHolder actingRoleHolder;

  @GET
  @ApiSubset({WEB_APP, MOBILE_APP})
  @Operation(
      operationId = "getDataProductsPaginated",
      description = "Retrieves a paginated list of all available data products. Accessible to users with the "
          + "provider, and admin. Supports pagination.")
  @Parameter(name = "resourceQueryDto",
      description = "Query parameters",
      schema = @Schema(implementation = ResourceQueryDto.class))
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({ADMIN_ROLE, PROVIDER_ROLE})
  @EnableActingRoleHolder
  public PageResponseDto<DataProductDto> getDataProductsPaginated(
      @BeanParam @Valid ResourceQueryDto resourceQueryDto,
      @Context HttpHeaders headers
  ) {
    SupportedLanguage language = headers.getAcceptableLanguages()
        .stream()
        .findFirst()
        .map(Locale::getLanguage)
        .map(SupportedLanguage::from)
        .orElse(SupportedLanguage.DE);

    return switch (actingRoleHolder.getRole()) {
      case ADMIN -> dataProductService.getDataProductsPaged(resourceQueryDto, language);
      case PROVIDER -> dataProductService.getProviderDataProductsPaged(resourceQueryDto, identity.getUidOrElseThrow(), language);
      default -> throw new ForbiddenException();
    };
  }

  @GET
  @Path("{id}")
  @ApiSubset({WEB_APP})
  @Operation(
      operationId = "getDataProduct",
      description = "Retrieves a single data product. Accessible to users with the provider and admin role"
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({ADMIN_ROLE, PROVIDER_ROLE})
  @EnableActingRoleHolder
  public DataProductDto getDataProduct(@PathParam("id") UUID dataProductId) {
    return dataProductService.getProductById(dataProductId);
  }
}

