package ch.agridata.agreement.service;

import ch.agridata.agreement.dto.DataRequestTerminationResultDto;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.product.api.DataProductApi;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Terminates data requests whose termination period has expired. Each data request is terminated in its own transaction, so that a
 * failure for one data request does not prevent the others from being terminated.
 *
 * <p>Every status change is audited and logged with the data request, the data provider, the data consumer and the time of the change.
 *
 * @CommentLastReviewed 2026-11-10
 */

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DataRequestTerminator {

  private final DataRequestRepository dataRequestRepository;
  private final DataRequestStateService dataRequestStateService;
  private final DataProductApi dataProductApi;
  private final AuditingService auditingService;
  private final Clock clock;

  public DataRequestTerminationResultDto terminateAllExpired() {
    var startedAt = System.nanoTime();
    var expiredIds = QuarkusTransaction.requiringNew().call(
        () -> dataRequestRepository.findTerminationPeriodExpired(LocalDate.now(clock)).stream()
            .map(DataRequestEntity::getId)
            .toList()
    );

    var terminatedCount = 0;
    var failedCount = 0;
    for (var id : expiredIds) {
      try {
        var terminated = QuarkusTransaction.requiringNew().call(() -> terminateSingleDataRequest(id));
        terminatedCount++;
        logTerminated(terminated);
      } catch (RuntimeException e) {
        failedCount++;
        log.error("failed to terminate data request id={}", id, e);
      }
    }

    return DataRequestTerminationResultDto.builder()
        .terminatedCount(terminatedCount)
        .failedCount(failedCount)
        .durationMs((System.nanoTime() - startedAt) / 1_000_000)
        .build();
  }

  private TerminatedDataRequestIdentifiers terminateSingleDataRequest(UUID id) {
    var entity = dataRequestRepository.findById(id);
    dataRequestStateService.transitionToTerminated(entity);
    auditingService.logDataRequestTerminated(id);

    return new TerminatedDataRequestIdentifiers(
        id, entity.getHumanFriendlyId(), entity.getDataSourceSystemId(), entity.getDataConsumerUid(), LocalDateTime.now(clock)
    );
  }

  /**
   * Runs after the transaction has committed: the provider lookup only enriches the log line and must never undo a termination.
   */
  private void logTerminated(TerminatedDataRequestIdentifiers terminated) {
    log.info(
        "terminated data request id={} humanFriendlyId={} dataProviderUid={} dataConsumerUid={} at={}",
        terminated.id(), terminated.humanFriendlyId(), findProviderUid(terminated), terminated.dataConsumerUid(), terminated.at()
    );
  }

  private String findProviderUid(TerminatedDataRequestIdentifiers terminated) {
    if (terminated.dataSourceSystemId() == null) {
      return null;
    }
    try {
      return dataProductApi.getDataSourceSystem(terminated.dataSourceSystemId()).dataProvider().uid();
    } catch (RuntimeException e) {
      log.warn("data provider of terminated data request id={} could not be resolved for logging", terminated.id(), e);
      return null;
    }
  }

  private record TerminatedDataRequestIdentifiers(
      UUID id,
      String humanFriendlyId,
      UUID dataSourceSystemId,
      String dataConsumerUid,
      LocalDateTime at
  ) {

  }
}
