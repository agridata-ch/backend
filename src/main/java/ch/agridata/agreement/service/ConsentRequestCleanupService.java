package ch.agridata.agreement.service;

import ch.agridata.agis.api.AgisApi;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;

/**
 * Service responsible for terminating obsolete consent requests based on recent
 * farm ownership changes and farm deletions reported by AGIS.
 *
 * <p>The cleanup process works as follows:
 * <ul>
 *   <li>Fetches farm ownership mutations for the last two completed days
 *       (yesterday and the day before).</li>
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
 * performed in batches to ensure scalability.
 *
 * @CommentLastReviewed 2026-03-02
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ConsentRequestCleanupService {

  private static final int BATCH_SIZE = 1000;

  private final AgisApi agisApi;
  private final ConsentRequestTerminator consentRequestTerminator;
  private final Clock clock;

  public long cleanupConsentRequestsFromYesterdayAndDayBefore() {
    LocalDate today = LocalDate.now(clock);
    LocalDate fromInclusive = today.minusDays(2);
    LocalDate toInclusive = today.minusDays(1);

    var currentFarmOwnerships =
        agisApi.fetchFarmMutations(fromInclusive, toInclusive).stream()
            .map(dto -> new ConsentRequestRepository.BurUidPair(dto.bur(), dto.uid()))
            .toList();

    var deletedBurs = agisApi.fetchFarmDeletions(fromInclusive, toInclusive);

    return consentRequestTerminator.terminateFor(currentFarmOwnerships, deletedBurs, BATCH_SIZE);
  }

}
