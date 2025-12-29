package ch.agridata.user.service;

import ch.agridata.user.dto.UidDto;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Aggregates and returns the set of UIDs a user is authorised to access. Combines farmer UIDs resolved from the
 * KtIdP with equid owner UIDs resolved from the AGATE login and de-duplicates by UID
 *
 * @CommentLastReviewed 2025-12-29
 */

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class UidAuthorizationService {

  private final FarmerUidProvider farmerUidProvider;
  private final EquidOwnerUidProvider equidOwnerUidProvider;

  public List<UidDto> getAuthorizedUids(@Nullable String ktIdP, @Nullable String agateLoginId) {
    var result = new HashMap<String, UidDto>();

    Optional.ofNullable(ktIdP)
        .map(farmerUidProvider::getAuthorizedUids)
        .orElse(List.of())
        .forEach(uidDto -> result.put(uidDto.uid(), uidDto));

    Optional.ofNullable(agateLoginId)
        .map(equidOwnerUidProvider::getAuthorizedUids)
        .orElse(List.of())
        .forEach(uidDto -> result.putIfAbsent(uidDto.uid(), uidDto));

    return result.values().stream().toList();
  }
}
