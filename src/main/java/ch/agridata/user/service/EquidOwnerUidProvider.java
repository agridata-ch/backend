package ch.agridata.user.service;

import static ch.agridata.user.dto.LegalFormEnum.EQUIDENEIGENTUEMER;

import ch.agridata.tvd.api.TvdApi;
import ch.agridata.tvd.dto.TvdEquidOwnerUidDto;
import ch.agridata.user.dto.UidDto;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

/**
 * Provides authorized UID information for equid owners based on an AGATE login ID. Fetches legal unit data via
 * {@link TvdApi}, maps the response to {@link UidDto} entries, and applies fault tolerance with retry, timeout,
 * and a fallback to an empty result when the TVD service is unavailable.
 *
 * @CommentLastReviewed 2025-12-29
 */

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class EquidOwnerUidProvider {

  @ConfigProperty(name = "agridata.tvd-equid-owner-dropdown.enabled", defaultValue = "false")
  boolean featureEnabled;

  private final TvdApi tvdApi;

  @Retry
  @Timeout(value = 2, unit = ChronoUnit.SECONDS)
  @Fallback(fallbackMethod = "fallbackAuthorizedUids")
  public List<UidDto> getAuthorizedUids(@NonNull String agateLoginId) {
    // TODO: Remove this feature flag once it’s enabled in all environments.
    if (!featureEnabled) {
      return Collections.emptyList();
    }
    var uids = tvdApi.fetchEquidOwnerLegalUnits(agateLoginId);
    return uids.stream()
        .map(this::buildDto)
        .toList();
  }

  @SuppressWarnings("unused")
  private List<UidDto> fallbackAuthorizedUids(@NonNull String agateLoginId, Throwable t) {
    log.error("TvdApi is not available. Returning empty list for agateLoginId={}", agateLoginId, t);
    return List.of();
  }

  private UidDto buildDto(TvdEquidOwnerUidDto.Data tvdResponse) {
    return UidDto.builder()
        .uid(tvdResponse.uid())
        .name(tvdResponse.getDisplayName())
        .legalFormCode(EQUIDENEIGENTUEMER)
        .build();
  }

}
