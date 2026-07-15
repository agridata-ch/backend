package ch.agridata.user.mapper;

import ch.agridata.user.dto.AgbRevisionDto;
import ch.agridata.user.persistence.AgbRevisionEntity;
import org.mapstruct.Mapper;

/**
 * This mapper converts between AgbRevisionEntity and AgbRevisionDto.
 *
 * @CommentLastReviewed 2026-07-16
 */

@Mapper(componentModel = "jakarta")
public interface AgbRevisionMapper {
  AgbRevisionDto toDto(AgbRevisionEntity agbRevisionEntity);
}
