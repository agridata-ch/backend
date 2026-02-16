package ch.agridata.product.mapper;

import ch.agridata.product.dto.DataSourceSystemDto;
import ch.agridata.product.persistence.DataSourceSystemEntity;
import org.mapstruct.Mapper;

/**
 * Maps between {@link DataSourceSystemEntity} and its DTO representation. It ensures that data source system
 * information is consistently transformed from the persistence layer to API responses.
 *
 * @CommentLastReviewed 2026-02-17
 */

@Mapper(componentModel = "jakarta")
public interface DataSourceSystemEntityMapper {
  DataSourceSystemDto toDto(DataSourceSystemEntity dataSourceSystemEntity);
}
