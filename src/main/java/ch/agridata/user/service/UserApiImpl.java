package ch.agridata.user.service;

import ch.agridata.user.api.UserApi;
import ch.agridata.user.dto.AdminUserDto;
import ch.agridata.user.dto.BurDto;
import ch.agridata.user.dto.UidDto;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Implements the participant API by delegating to authorization services.
 *
 * @CommentLastReviewed 2026-05-06
 */
@ApplicationScoped
@RequiredArgsConstructor
public class UserApiImpl implements UserApi {

  private final BurAuthorizationService burAuthorizationService;
  private final UidAuthorizationService uidAuthorizationService;
  private final UserService userService;

  @Override
  public List<BurDto> getAuthorizedBurs(@NonNull String uid) {
    return burAuthorizationService.getAuthorizedBurs(uid);
  }

  @Override
  public List<UidDto> getAuthorizedUids(@Nullable String ktIdP, @Nullable String agateLoginId) {
    return uidAuthorizationService.getAuthorizedUids(ktIdP, agateLoginId);
  }

  @Override
  public List<AdminUserDto> getAdminUsers() {
    return userService.getAdminUsers();
  }
}
