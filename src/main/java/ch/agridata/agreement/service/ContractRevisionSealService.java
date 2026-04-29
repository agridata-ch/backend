package ch.agridata.agreement.service;

import ch.agridata.agreement.dto.SealAttemptStateEnum;
import ch.agridata.agreement.mapper.ContractRevisionMapper;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.agreement.persistence.ContractRevisionEntity.SealAttemptState;
import ch.agridata.agreement.persistence.ContractRevisionRepository;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.bit.api.BitSignatureApi;
import ch.agridata.common.security.AgridataSecurityIdentity;
import io.quarkus.arc.Arc;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.context.ManagedExecutor;

/**
 * Seals a contract revision PDF using the BIT evidence Signing API. Sealing is performed
 * asynchronously; use {@link #getSealState} to poll for the result.
 *
 * @CommentLastReviewed 2026-04-14
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ContractRevisionSealService {

  private static final Duration STUCK_TIMEOUT = Duration.ofMinutes(5);

  private final BitSignatureApi bitSignatureApi;
  private final ContractRevisionRepository contractRevisionRepository;
  private final ContractRevisionQueryService contractRevisionQueryService;
  private final ContractRevisionMapper contractRevisionMapper;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final ManagedExecutor managedExecutor;
  private final ContractRevisionStorageService contractRevisionStorageService;
  private final ContractRevisionPdfService contractRevisionPdfService;

  public SealAttemptStateEnum getSealState(UUID contractRevisionId, boolean longPolling) {
    long deadline = System.currentTimeMillis() + 10_000;
    while (true) {
      SealAttemptStateEnum state = QuarkusTransaction.requiringNew().call(() -> {
        var contract = contractRevisionQueryService.getWithAccessCheck(contractRevisionId);
        return contractRevisionMapper.toSealAttemptStateEnum(contract.getSealState());
      });
      if (!longPolling
          || state != SealAttemptStateEnum.IN_PROGRESS
          || System.currentTimeMillis() >= deadline) {
        return state;
      }
      try {
        //noinspection BusyWait - intentional: virtual thread releases platform thread during sleep
        Thread.sleep(1_000);
      } catch (InterruptedException _) {
        Thread.currentThread().interrupt();
        return state;
      }
    }
  }

  public void sealAsync(UUID contractRevisionId, String adminGlobalId) {
    QuarkusTransaction.requiringNew().run(() -> {
      ContractRevisionEntity entity = contractRevisionRepository.findByIdOptional(contractRevisionId)
          .orElseThrow(NotFoundException::new);
      if (isInProgress(entity)) {
        throw new IllegalStateException("seal process is already running for contractRevisionId=" + contractRevisionId);
      }
      if (entity.getDataRequest().getStateCode() != DataRequestEntity.DataRequestStateEnum.TO_BE_ACTIVATED) {
        throw new IllegalStateException(
            "seal process cannot be started for data request in state=" + entity.getDataRequest().getStateCode());
      }
      entity.setSealState(SealAttemptState.IN_PROGRESS);
      entity.setSealStartedAt(LocalDateTime.now());
    });
    runAsyncAsUser(agridataSecurityIdentity.getUserId(), () -> performSeal(contractRevisionId, adminGlobalId));
  }

  /**
   * Captures the current user ID from the active HTTP request context and submits the given task
   * to the managed executor. The task runs in a fresh request context with the captured user ID
   * set as {@code scheduledJobUserId}, allowing the {@code AuditingEntityListener} to set
   * {@code modifiedBy} correctly even though the original request context is no longer active.
   */
  private void runAsyncAsUser(UUID userId, Runnable task) {
    managedExecutor.submit(() -> {
      var requestContext = Arc.container().requestContext();
      requestContext.activate();
      try {
        agridataSecurityIdentity.setRunAsUserId(userId);
        task.run();
      } finally {
        requestContext.deactivate();
      }
    });
  }

  private boolean isInProgress(ContractRevisionEntity entity) {
    SealAttemptState state = entity.getSealState();
    if (state != SealAttemptState.IN_PROGRESS) {
      return false;
    }
    return entity.getSealStartedAt() != null
        && entity.getSealStartedAt().isAfter(LocalDateTime.now().minus(STUCK_TIMEOUT));
  }

  private void performSeal(UUID contractRevisionId, String adminGlobalId) {
    try {
      byte[] unsealedPdf = contractRevisionStorageService.download(contractRevisionId);
      byte[] sealedPdf = bitSignatureApi.sign(unsealedPdf, adminGlobalId);
      contractRevisionStorageService.upload(contractRevisionId, sealedPdf);
      updateSealState(contractRevisionId, SealAttemptState.COMPLETED);
    } catch (Exception e) {
      log.error("BIT seal failed for contractRevisionId={}: {}", contractRevisionId, e.getMessage(), e);
      updateSealState(contractRevisionId, SealAttemptState.FAILED);
    }
  }

  private void updateSealState(UUID contractRevisionId, SealAttemptState state) {
    QuarkusTransaction.requiringNew().run(() -> {
      ContractRevisionEntity entity = contractRevisionRepository.findById(contractRevisionId);
      if (entity != null) {
        entity.setSealState(state);
      }
    });
  }
}
