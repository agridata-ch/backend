package ch.agridata.user.service;

import ch.agridata.user.api.UserApi;
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
 * @CommentLastReviewed 2025-08-25
 */
@ApplicationScoped
@RequiredArgsConstructor
public class UserApiImpl implements UserApi {

  private final BurAuthorizationService burAuthorizationService;
  private final UidAuthorizationService uidAuthorizationService;

  @Override
  public List<BurDto> getAuthorizedBurs(@NonNull String uid) {
    return burAuthorizationService.getAuthorizedBurs(uid);
  }

  @Override
  public List<UidDto> getAuthorizedUids(@Nullable String ktIdP, @Nullable String agateLoginId) {
    return uidAuthorizationService.getAuthorizedUids(ktIdP, agateLoginId);
  }
}
