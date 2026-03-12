package ch.agridata.tvd.service;

import ch.agridata.common.security.EquidOwnerChecker;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Determines equid ownership by querying the TVD API for equid legal units.
 *
 * @CommentLastReviewed 2026-03-12
 */
@ApplicationScoped
@RequiredArgsConstructor
public class TvdEquidOwnerChecker implements EquidOwnerChecker {

  private final TvdQueryService tvdQueryService;

  @Override
  public boolean isEquidOwner(@NonNull String agateLoginId) {
    return !tvdQueryService.fetchEquidOwnerLegalUnits(agateLoginId).isEmpty();
  }
}
