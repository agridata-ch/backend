package ch.agridata.product.mapper;

import ch.agridata.product.dto.DataProviderDto;
import ch.agridata.product.persistence.DataProviderEntity;
import org.mapstruct.Mapper;

/**
 * Maps between DataProviderEntity and its DTO representations. It ensures dataProvider metadata is accurately transformed for both
 * persistence and API responses.
 *
 * @CommentLastReviewed 2026-06-11
 */
@Mapper(componentModel = "jakarta")
public interface DataProviderMapper {
  DataProviderDto toDto(DataProviderEntity dataProviderEntity);
}
