package ch.agridata.user.service;

import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.common.exceptions.UidMissingException;
import ch.agridata.common.exceptions.UidProviderUnavailableException;
import ch.agridata.user.dto.UidDto;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jspecify.annotations.NonNull;

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
  private final ManagedExecutor managedExecutor;

  public List<UidDto> getAuthorizedUids(@Nullable String ktIdP, @Nullable String agateLoginId) {
    var farmerFuture = ktIdP == null
        ? CompletableFuture.completedFuture(new ProviderResult(true, List.of()))
        : CompletableFuture.supplyAsync(() -> getFarmerServiceAuthorizedUids(ktIdP), managedExecutor);
    var equidFuture = agateLoginId == null
        ? CompletableFuture.completedFuture(new ProviderResult(true, List.of()))
        : CompletableFuture.supplyAsync(() -> getEquidOwnerServiceAuthorizedUids(agateLoginId), managedExecutor);

    var farmerResult = farmerFuture.join();
    var equidResult = equidFuture.join();

    var result = new HashMap<String, UidDto>();
    farmerResult.uids().forEach(uidDto -> result.put(uidDto.uid(), uidDto));
    equidResult.uids().forEach(uidDto -> result.putIfAbsent(uidDto.uid(), uidDto));

    if (!result.isEmpty()) {
      return result.values().stream().toList();
    }

    var anyProviderUnavailable = !farmerResult.available() || !equidResult.available();
    if (anyProviderUnavailable) {
      log.error("No UID found with one external service unavailable for ktIdP={} and agateLoginId={}", ktIdP, agateLoginId);
      throw new UidProviderUnavailableException(
          "Could not determine authorized UIDs because one or more external services are unavailable"
      );
    }
    log.error("No UIDs found for ktIdP={} and agateLoginId={}, sync may still be in progress", ktIdP, agateLoginId);
    throw new UidMissingException("No UID is currently associated with this account; synchronization may still be in progress");
  }

  private ProviderResult getFarmerServiceAuthorizedUids(@NonNull String ktIdP) {
    try {
      return new ProviderResult(true, farmerUidProvider.getAuthorizedUids(ktIdP));
    } catch (ExternalWebServiceException ex) {
      log.warn("AGIS unavailable while resolving authorized UIDs for ktIdP={}", ktIdP, ex);
      return new ProviderResult(false, List.of());
    }
  }

  private ProviderResult getEquidOwnerServiceAuthorizedUids(@NonNull String agateLoginId) {
    try {
      return new ProviderResult(true, equidOwnerUidProvider.getAuthorizedUids(agateLoginId));
    } catch (ExternalWebServiceException ex) {
      log.warn("TVD unavailable while resolving authorized UIDs for agateLoginId={}", agateLoginId, ex);
      return new ProviderResult(false, List.of());
    }
  }

  private record ProviderResult(boolean available, List<UidDto> uids) {
  }
}
