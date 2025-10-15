package ch.agridata.agreement.mapper;

import ch.agridata.agreement.dto.ConsentRequestConsumerViewDto;
import ch.agridata.agreement.dto.ConsentRequestCreatedDto;
import ch.agridata.agreement.dto.ConsentRequestProducerViewDto;
import ch.agridata.agreement.dto.ConsentRequestStateEnum;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.persistence.ConsentRequestEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.ValueMapping;

/**
 * Transforms consent request data between transport and storage formats. It supports seamless communication between application layers.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Mapper(componentModel = "jakarta", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ConsentRequestMapper {

  static LocalDate toLocalDate(LocalDateTime dateTime) {
    return dateTime != null ? dateTime.toLocalDate() : null;
  }

  @Mapping(target = "id", source = "entity.id")
  @Mapping(target = "dataRequest", source = "dataRequest")
  @Mapping(target = "stateCode", source = "entity.stateCode")
  @Mapping(target = "showStateAsMigrated", source = "entity.showStateAsMigrated")
  ConsentRequestProducerViewDto toConsentRequestProducerViewDto(ConsentRequestEntity entity, DataRequestDto dataRequest);

  ConsentRequestConsumerViewDto toConsentRequestConsumerViewDto(ConsentRequestEntity entity);

  @ValueMapping(source = "NOT_CREATED", target = MappingConstants.NULL)
  ConsentRequestEntity.StateEnum toEntityStateEnum(ConsentRequestStateEnum stateEnum);

  ConsentRequestCreatedDto toConsentRequestCreatedDto(ConsentRequestEntity entity, boolean isCreated);
}
