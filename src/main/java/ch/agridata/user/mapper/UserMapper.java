package ch.agridata.user.mapper;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.user.dto.UserInfoDto;
import ch.agridata.user.dto.UserPreferencesDto;
import ch.agridata.user.persistence.UserEntity;
import ch.agridata.user.persistence.UserEntityPreferencesDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for converting between {@link UserEntity} persistence objects and {@link UserInfoDto} data transfer objects.
 *
 * @CommentLastReviewed 2025-08-27
 */

@Mapper(componentModel = "jakarta", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

  UserInfoDto toUserInfoDto(UserEntity userEntity);

  PageResponseDto<UserInfoDto> toPagedUserInfoDto(PageResponseDto<UserEntity> pagedProducerEntities);


  UserPreferencesDto toUserPreferencesDto(UserEntityPreferencesDto userEntityPreferences);

  UserEntityPreferencesDto toUserPreferenceEntity(UserPreferencesDto dto);
}
