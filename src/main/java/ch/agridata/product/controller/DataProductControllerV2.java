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
import ch.agridata.product.dto.DataProductStateEnum;
import ch.agridata.product.dto.DataProductUpdateDto;
import ch.agridata.product.service.DataProductMutationService;
import ch.agridata.product.service.DataProductQueryService;
import ch.agridata.product.service.DataProductStateService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
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
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestResponse;

/**
 * The DataProductControllerV2 class serves as a REST endpoint controller for managing
 * data products. It provides operations for retrieving, creating, updating, and managing
 * the status of data products. The controller supports role-based access control and
 * different functionalities for users with admin and provider roles.
 *
 * @CommentLastReviewed 2026-06-11
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
  private final DataProductQueryService dataProductQueryService;
  private final AgridataSecurityIdentity identity;
  private final ActingRoleHolder actingRoleHolder;
  private final DataProductStateService dataProductStateService;
  private final DataProductMutationService dataProductMutationService;

  @GET
  @Path("{id}")
  @ApiSubset({WEB_APP})
  @Operation(
      operationId = "getDataProduct",
      description = "Retrieves a single data product. Acessible to users with the provider and admin role"
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({ADMIN_ROLE, PROVIDER_ROLE})
  @EnableActingRoleHolder
  public DataProductDto getDataProduct(@PathParam("id") UUID dataProductId) {
    return switch (actingRoleHolder.getRole()) {
      case ADMIN -> dataProductQueryService.getDataProductAsAdmin(dataProductId);
      case PROVIDER -> dataProductQueryService.getDataProductAsProvider(dataProductId);
      default -> throw new ForbiddenException();
    };
  }

  @GET
  @ApiSubset({WEB_APP, MOBILE_APP})
  @Operation(
      operationId = "getDataProductsPaginated",
      description = "Retrieves a paginated list of all available data products. Accessible to users with the "
          + "producer, and admin. Supports pagination.")
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
      case ADMIN -> dataProductQueryService.getDataProductsPagedAsAdmin(resourceQueryDto, language);
      case PROVIDER -> dataProductQueryService.getDataProductsPagedAsProvider(resourceQueryDto, identity.getUidOrElseThrow(),
          language);
      default -> throw new ForbiddenException();
    };
  }

  @POST
  @ApiSubset({WEB_APP})
  @Operation(
      operationId = "createDataProductDraft",
      description = "Creates a new data product in draft status. Only accessible to users with the admin or provider role."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @ResponseStatus(RestResponse.StatusCode.CREATED)
  @RolesAllowed({PROVIDER_ROLE, ADMIN_ROLE})
  @EnableActingRoleHolder
  public DataProductDto createDataProductDraft(@Valid DataProductUpdateDto dataProductUpdateDto) {
    return switch (actingRoleHolder.getRole()) {
      case PROVIDER -> dataProductMutationService.addDataProductDraftAsProvider(dataProductUpdateDto);
      case ADMIN -> dataProductMutationService.addDataProductDraftAsAdmin(dataProductUpdateDto);
      default -> throw new ForbiddenException();
    };
  }

  @PUT
  @Path("/{id}")
  @ApiSubset({WEB_APP})
  @Operation(
      operationId = "updateDataProductDraft",
      description = "Updates the details of an existing data product."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({PROVIDER_ROLE, ADMIN_ROLE})
  @EnableActingRoleHolder
  public DataProductDto updateDataProductDraft(
      @PathParam("id") UUID dataProductId,
      @Valid DataProductUpdateDto dataProductUpdateDto
  ) {
    return switch (actingRoleHolder.getRole()) {
      case PROVIDER -> dataProductMutationService.updateDataProductDraftAsProvider(dataProductId, dataProductUpdateDto);
      case ADMIN -> dataProductMutationService.updateDataProductDraftAsAdmin(dataProductId, dataProductUpdateDto);
      default -> throw new ForbiddenException();
    };
  }

  @PUT
  @ApiSubset({WEB_APP})
  @Path("/{id}/status")
  @Operation(
      operationId = "setDataProductStatus",
      description = "Sets the status of a data product. Only accessible to the provider who owns the data product. Admins on the other "
          + "hand can set the state to any data product. Only specific transitions are allowed."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({PROVIDER_ROLE, ADMIN_ROLE})
  @EnableActingRoleHolder
  public DataProductDto setDataProductStatus(
      @PathParam("id") UUID dataProductId,
      @Valid DataProductStateEnum stateCode
  ) {
    return switch (actingRoleHolder.getRole()) {
      case PROVIDER -> dataProductStateService.setStateAsProvider(dataProductId, stateCode);
      case ADMIN -> dataProductStateService.setStateAsAdmin(dataProductId, stateCode);
      default -> throw new ForbiddenException();
    };
  }
}