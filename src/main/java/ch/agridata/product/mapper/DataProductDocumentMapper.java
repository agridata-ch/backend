package ch.agridata.product.mapper;

import ch.agridata.product.dto.DataProductDocumentMetadataDto;
import ch.agridata.product.persistence.DataProductDocumentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between DataProductDocumentEntity and DataProductDocumentMetadataDto.
 *
 * @CommentLastReviewed 2026-07-09
 */

@Mapper(componentModel = "jakarta")
public interface DataProductDocumentMapper {

  @Mapping(target = "fileName", source = "originalFilename")
  DataProductDocumentMetadataDto toDto(DataProductDocumentEntity entity);
}
