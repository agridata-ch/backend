package ch.agridata.common.security;

import lombok.NonNull;

/**
 * SPI for determining whether an authenticated user is an equid owner. Equid owners receive the
 * producer role via {@link RolesAugmentor}.
 *
 * <p>The implementation is discovered via CDI and invoked on each authenticated request,
 * unless the producer role is already present in the token.
 *
 * @CommentLastReviewed 2026-03-12
 */
public interface EquidOwnerChecker {

  /**
   * Returns {@code true} if the given AGATE login ID belongs to an equid owner.
   *
   * @param agateLoginId the AGATE login ID of the authenticated user
   * @return {@code true} if the user is an equid owner
   */
  boolean isEquidOwner(@NonNull String agateLoginId);
}
