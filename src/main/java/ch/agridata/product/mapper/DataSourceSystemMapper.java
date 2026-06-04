package ch.agridata.product.mapper;

import ch.agridata.product.dto.DataSourceSystemDto;
import ch.agridata.product.persistence.DataSourceSystemEntity;
import org.mapstruct.Mapper;

/**
 * Maps between {@link DataSourceSystemEntity} and its DTO representation. It ensures that data source system
 * information is consistently transformed from the persistence layer to API responses.
 *
 * @CommentLastReviewed 2026-06-11
 */

@Mapper(componentModel = "jakarta")
public interface DataSourceSystemMapper {
  DataSourceSystemDto toDto(DataSourceSystemEntity dataSourceSystemEntity);
}
