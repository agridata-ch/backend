package ch.agridata.product.controller;

import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.product.controller.PublicDataProductController.PATH;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.openapi.ApiSubset;
import ch.agridata.product.dto.DataProductDocumentMetadataDto;
import ch.agridata.product.dto.PublicDataProductDto;
import ch.agridata.product.service.DataProductDocumentService;
import ch.agridata.product.service.DataProductQueryService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Controller for managing public access to data products.
 * Provides endpoints to retrieve information about publicly available data products.
 * This controller is accessible to all users.
 *
 * @CommentLastReviewed 2026-08-03
 */

@Path(PATH)
@RequiredArgsConstructor
@Tag(name = "Public Data Products", description = "Endpoints for public data products")
@RunOnVirtualThread
public class PublicDataProductController {
  public static final String PATH = "/public/api/products/v2";

  private final DataProductQueryService dataProductQueryService;
  private final DataProductDocumentService dataProductDocumentService;

  @GET
  @ApiSubset({WEB_APP})
  @Operation(
      operationId = "getPublicDataProductsPaginated",
      description = "Retrieves a paginated list of all publicly available data products. Publicly accessible."
  )
  @Parameter(
      name = "resourceQueryDto",
      description = "Query parameters",
      schema = @Schema(implementation = ResourceQueryDto.class)
  )
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  public PageResponseDto<PublicDataProductDto> getPublicDataProductsPaginated(
      @BeanParam @Valid ResourceQueryDto queryDto
  ) {
    return dataProductQueryService.getPublicDataProductsPaged(queryDto);
  }

  @GET
  @ApiSubset({WEB_APP})
  @Path("/{id}/documents")
  @Operation(
      operationId = "getPublicDataProductDocumentsMetadata",
      description = "Retrieves metadata for all successfully virus-scanned documents of a publicly available data product. "
          + "Publicly accessible."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  public List<DataProductDocumentMetadataDto> getPublicDataProductDocumentsMetadata(
      @PathParam("id") UUID dataProductId
  ) {
    return dataProductDocumentService.getPublicDataProductDocumentsMetadata(dataProductId);
  }
}
