package ch.agridata.notification.mapper;

import ch.agridata.notification.dto.TargetTypeCodeEnum;
import org.mapstruct.Mapper;

/**
 * The NotificationTargetTypeMapper interface is responsible for mapping between the
 * DTO-layer {@code TargetTypeCodeEnum} and the persistence-layer {@code TargetTypeCodeEnum}.
 * This is typically used to convert enum values when persisting or retrieving data regarding
 * notification targets.
 */

@Mapper(componentModel = "jakarta")
public interface NotificationTargetTypeMapper {

  ch.agridata.notification.persistence.TargetTypeCodeEnum toEntityEnum(TargetTypeCodeEnum dtoEnum);

  TargetTypeCodeEnum toDtoEnum(ch.agridata.notification.persistence.TargetTypeCodeEnum entityEnum);
}
