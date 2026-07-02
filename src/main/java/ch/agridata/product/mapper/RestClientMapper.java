package ch.agridata.product.mapper;

import ch.agridata.product.dto.RestClientDto;
import ch.agridata.product.persistence.RestClientEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps between {@code RestClientEntity} and its DTO representation {@code RestClientDto}.
 * This interface is designed to facilitate the transformation of entities to DTOs for API responses
 * or other uses, while maintaining a consistent mapping logic.
 *
 * @CommentLastReviewed 2026-06-11
 */

@Mapper(componentModel = "jakarta", uses = RestClientUrlResolver.class)
public interface RestClientMapper {

  @Mapping(target = "url", source = "entity", qualifiedByName = "resolveRestClientUrl")
  RestClientDto toDto(RestClientEntity entity);
}
