package ch.agridata.agreement.service;

import ch.agridata.agreement.persistence.ConsentRequestRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Coordinates termination of obsolete consent request BUR–UID relationships.
 *
 * <p>This service determines which consent requests must have their BUR–UID
 * relation terminated based on two independent sources:
 * <ul>
 *   <li>Changed farm ownerships, where the stored BUR–UID pairing no longer
 *       matches the current AGIS ownership.</li>
 *   <li>Deleted farms, where the associated BUR no longer exists.</li>
 * </ul>
 *
 * <p>Termination is performed in batches by delegating to repository methods
 * that update the persistence layer and return the affected records. The
 * service ensures that:
 * <ul>
 *   <li>Duplicate consent request identifiers are removed before termination.</li>
 *   <li>Termination timestamps are generated consistently using the injected {@link Clock}.</li>
 *   <li>The operation is idempotent, as only active BUR–UID relations
 *       (i.e. those without a termination timestamp) are affected.</li>
 * </ul>
 *
 * <p>For auditability and operational transparency, the service logs each
 * terminated consent request together with its BUR–UID pair.
 *
 * <p>The entire operation runs within a single transaction to guarantee
 * consistency between identification and termination of affected records.
 *
 * @CommentLastReviewed 2026-03-02
 */

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ConsentRequestTerminator {

  private final ConsentRequestRepository consentRequestRepository;
  private final Clock clock;

  @Transactional
  public long terminateFor(
      List<ConsentRequestRepository.BurUidPair> ownerships,
      List<String> deletedBurs,
      int batchSize
  ) {
    List<UUID> ownershipIds =
        consentRequestRepository.findIdsToTerminateByChangedFarmOwnerships(ownerships);

    List<UUID> deletionIds =
        consentRequestRepository.findIdsToTerminateByDataProducerBurs(deletedBurs, batchSize);

    List<UUID> allIds = Stream.concat(ownershipIds.stream(), deletionIds.stream())
        .distinct()
        .toList();

    if (allIds.isEmpty()) {
      return 0;
    }

    LocalDateTime terminatedAt = LocalDateTime.now(clock);

    // Log what actually changed
    var terminated = consentRequestRepository.terminateByIdsReturningPairs(allIds, batchSize, terminatedAt);

    if (!terminated.isEmpty()) {
      // Keep it readable; avoid giant single log lines if this can be large
      terminated.forEach(t ->
          log.info("terminated consent request id={} bur={} uid={}", t.id(), t.bur(), t.uid())
      );
    }

    return terminated.size();
  }
}
