package ch.agridata.user.mapper;

import ch.agridata.user.dto.AgbRevisionDto;
import ch.agridata.user.dto.AgbRevisionSummaryDto;
import ch.agridata.user.dto.CreateAgbRevisionDto;
import ch.agridata.user.persistence.AgbRevisionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * This mapper converts between AgbRevisionEntity and AgbRevisionDto.
 *
 * @CommentLastReviewed 2026-07-16
 */
@Mapper(componentModel = "jakarta")
public interface AgbRevisionMapper {
  AgbRevisionDto toDto(AgbRevisionEntity agbRevisionEntity);

  AgbRevisionSummaryDto toSummaryDto(AgbRevisionEntity agbRevisionEntity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "validFrom", ignore = true)
  @Mapping(target = "validTo", ignore = true)
  AgbRevisionEntity toEntity(CreateAgbRevisionDto createAgbRevisionDto);
}
