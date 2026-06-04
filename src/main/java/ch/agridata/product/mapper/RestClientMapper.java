package ch.agridata.product.mapper;

import ch.agridata.product.dto.RestClientDto;
import ch.agridata.product.persistence.RestClientEntity;
import org.mapstruct.Mapper;

/**
 * Maps between {@code RestClientEntity} and its DTO representation {@code RestClientDto}.
 * This interface is designed to facilitate the transformation of entities to DTOs for API responses
 * or other uses, while maintaining a consistent mapping logic.
 *
 * @CommentLastReviewed 2026-06-11
 */

@Mapper(componentModel = "jakarta")
public interface RestClientMapper {
  RestClientDto toDto(RestClientEntity entity);
}
