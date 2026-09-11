package ch.agridata.agreement.service;

import ch.agridata.agis.api.AgisApi;
import ch.agridata.agreement.dto.ConsentRequestCleanupOutcomeDto;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;

/**
 * Service responsible for terminating obsolete consent requests based on farm ownership changes
 * and farm deletions reported by AGIS within a given time window.
 *
 * <p>The cleanup process works as follows:
 * <ul>
 *   <li>Fetches farm ownership mutations for the given window.</li>
 *   <li>Determines consent requests that became obsolete because the stored
 *       BUR–UID pairing no longer matches the current ownership.</li>
 *   <li>Fetches BURs of farms that were deleted in AGIS within the same time window.</li>
 *   <li>Determines consent requests whose BUR–UID relationship must be terminated because
 *       the associated farm no longer exists.</li>
 *   <li>Terminates all affected consent requests in batches by setting {@code uidBurRelationUntil}.</li>
 * </ul>
 *
 * <p>The service is designed to be safe for repeated execution: termination operations
 * are idempotent (only records with {@code uidBurRelationUntil = null} are affected) and
 * performed in batches to ensure scalability. Callers decide the window to evaluate; by default
 * (see {@link ConsentRequestCleanupRunner}) this is the last two completed days, but a caller may
 * pass an earlier window to catch up on days that were missed, e.g. after an outage.
 *
 * <p>The evaluated time window and the number of terminated consent requests are reported back,
 * so that callers can surface the result of a run.
 *
 * @CommentLastReviewed 2026-09-08
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ConsentRequestCleanupService {

  private static final int BATCH_SIZE = 1000;

  private final AgisApi agisApi;
  private final ConsentRequestTerminator consentRequestTerminator;

  public ConsentRequestCleanupOutcomeDto cleanup(LocalDate fromInclusive, LocalDate toInclusive) {
    var currentFarmOwnerships = agisApi.fetchFarmMutations(fromInclusive, toInclusive)
        .stream()
        .map(dto -> new ConsentRequestRepository.BurUidPair(dto.bur(), dto.uid()))
        .toList();

    var deletedBurs = agisApi.fetchFarmDeletions(fromInclusive, toInclusive);

    var terminatedCount = consentRequestTerminator.terminateFor(currentFarmOwnerships, deletedBurs, BATCH_SIZE);

    return ConsentRequestCleanupOutcomeDto.builder()
        .fromInclusive(fromInclusive)
        .toInclusive(toInclusive)
        .terminatedConsentRequestCount(terminatedCount)
        .build();
  }

}
