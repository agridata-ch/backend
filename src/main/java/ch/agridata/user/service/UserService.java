package ch.agridata.user.service;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.user.dto.UserInfoDto;
import ch.agridata.user.dto.UserPreferencesDto;
import ch.agridata.user.mapper.UserMapper;
import ch.agridata.user.persistence.UserRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Service responsible for updating user information in the database based on the currently authenticated identity.
 *
 * @CommentLastReviewed 2026-05-06
 */
@ApplicationScoped
@RequiredArgsConstructor
public class UserService {

  private final AgridataSecurityIdentity identity;
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @RolesAllowed({SUPPORT_ROLE})
  public PageResponseDto<UserInfoDto> getProducers(ResourceQueryDto resourceQueryDto) {
    var pagedProducerEntities = userRepository.findByRoleAtLastLogin(resourceQueryDto, PRODUCER_ROLE);
    return userMapper.toPagedUserInfoDto(pagedProducerEntities);
  }

  @Transactional
  public UserInfoDto updateUserData() {
    var user = userRepository.findById(identity.getUserId());

    var userInfo = identity.getUserInfoOrElseThrow();

    user.setKtIdP(userInfo.getString("KT_ID_P"));
    user.setUid(userInfo.getString("uid"));
    user.setEmail(userInfo.getString("email"));
    user.setGivenName(userInfo.getString("given_name"));
    user.setFamilyName(userInfo.getString("family_name"));
    user.setPhoneNumber(userInfo.getString("phone_number"));

    user.setRolesAtLastLogin(identity.getRoles());
    user.setLastLoginDate(LocalDateTime.now());

    var address = userInfo.getObject("address");
    if (address != null) {
      user.setAddressStreet(address.getString("street_address", null));
      user.setAddressLocality(address.getString("locality", null));
      user.setAddressPostalCode(address.getString("postal_code", null));
      user.setAddressCountry(address.getString("country", null));
    }

    return userMapper.toUserInfoDto(user);
  }

  public UserInfoDto getUserInfo(@NonNull String agateLoginId) {
    return userRepository.findByAgateLoginId(agateLoginId)
        .map(userMapper::toUserInfoDto)
        .orElseThrow(() -> new NotFoundException(agateLoginId));
  }

  @Transactional
  public void updateUserPreferences(@Valid UserPreferencesDto userPreferences) {
    var user = userRepository.findById(identity.getUserId());
    user.setUserPreferences(userMapper.toUserPreferenceEntity(userPreferences));
  }

  public List<UUID> getAdminUserIds() {
    return userRepository.findAllIdsByRoleAtLastLogin(ADMIN_ROLE);
  }
}
