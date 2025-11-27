package ch.agridata.user.service;

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
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;

/**
 * Service responsible for updating user information in the database based on the currently authenticated identity.
 *
 * @CommentLastReviewed 2025-08-27
 */
@ApplicationScoped
@RequiredArgsConstructor
public class UserService {

  private final AgridataSecurityIdentity identity;
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @RolesAllowed({SUPPORT_ROLE})
  public PageResponseDto<UserInfoDto> getProducers(ResourceQueryDto resourceQueryDto) {
    var pagedProducerEntities = userRepository.findByKtIdpNotNull(resourceQueryDto);
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

  public UserInfoDto getUserIdByKtIdP(String impersonatedKtIdP) {
    return userRepository.findByKtIdP(impersonatedKtIdP).map(userMapper::toUserInfoDto)
        .orElseThrow(() -> new IllegalArgumentException("No user found for ktIdP: " + impersonatedKtIdP));
  }

  @Transactional
  public void updateUserPreferences(@Valid UserPreferencesDto userPreferences) {
    var user = userRepository.findById(identity.getUserId());
    user.setUserPreferences(userMapper.toUserPreferenceEntity(userPreferences));
  }
}
