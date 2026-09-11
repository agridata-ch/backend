package ch.agridata.agreement.service;

import ch.agridata.agis.api.AgisApi;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.user.api.UserApi;
import ch.agridata.user.dto.BurDto;
import io.quarkus.arc.Arc;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Asynchronously creates LEGALLY_PERMITTED consent requests for fetches of consent-free data products,
 * so that producers gain visibility of accesses that legally require no consent.
 * Uses a bounded in-memory queue drained by a single worker thread, which serializes - and thereby
 * throttles - the AGIS lookups needed for BUR-to-UID resolution. Processing is best-effort: on queue
 * saturation, AGIS failure, or service restart entries are dropped (logged at ERROR) without ever
 * affecting the already-answered data transfer.
 *
 * @CommentLastReviewed 2026-09-01
 */
@ApplicationScoped
@Slf4j
public class ConsentRequestLegallyPermittedService {

  // Dedicated technical user ID recorded as createdBy on the auto-generated consent requests
  // (traceability/auditing). The corresponding user entry must exist in the users table.
  public static final UUID USER_ID_LEGALLY_PERMITTED_CONSENT = UUID.fromString("c39129a1-9035-4497-909f-3ee3488cf022");

  private final ConsentRequestCreationService consentRequestCreationService;
  private final AgisApi agisApi;
  private final UserApi userApi;
  private final AgridataSecurityIdentity identity;
  private final ExecutorService executor;

  public ConsentRequestLegallyPermittedService(
      ConsentRequestCreationService consentRequestCreationService,
      AgisApi agisApi,
      UserApi userApi,
      AgridataSecurityIdentity identity,
      @ConfigProperty(name = "agridata.agreement.legally-permitted-consent.queue-capacity") int queueCapacity) {
    this.consentRequestCreationService = consentRequestCreationService;
    this.agisApi = agisApi;
    this.userApi = userApi;
    this.identity = identity;
    // Single worker on purpose: keeps the AGIS BUR->UID lookups sequential so the external API is not hit in too many parallel bursts.
    this.executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(queueCapacity));
  }

  public void enqueueUidBased(UUID dataRequestId, String uid) {
    submit(dataRequestId, "uid=" + uid, () -> createForUid(dataRequestId, uid));
  }

  public void enqueueBurBased(UUID dataRequestId, String bur) {
    submit(dataRequestId, "bur=" + bur, () -> createForBur(dataRequestId, bur));
  }

  private void submit(UUID dataRequestId, String producer, Runnable work) {
    try {
      executor.execute(work);
    } catch (RejectedExecutionException _) {
      log.error("Legally permitted consent request dropped, queue saturated: dataRequestId={}, {}", dataRequestId, producer);
    }
  }

  private void createForUid(UUID dataRequestId, String uid) {
    runInRequestContext(dataRequestId, "uid=" + uid, () ->
        consentRequestCreationService.createLegallyPermittedConsentRequestIfMissing(dataRequestId, uid, null, null));
  }

  private void createForBur(UUID dataRequestId, String bur) {
    runInRequestContext(dataRequestId, "bur=" + bur, () -> {
      var uid = agisApi.fetchFarmForBur(bur)
          .orElseThrow(() -> new IllegalStateException("No farm found in AGIS for bur " + bur))
          .getUid();
      var relationSince = userApi.getAuthorizedBurs(uid).stream()
          .filter(authorizedBur -> bur.equals(authorizedBur.bur()))
          .map(BurDto::relationSince)
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("Bur " + bur + " is not among the authorized burs of uid " + uid));
      consentRequestCreationService.createLegallyPermittedConsentRequestIfMissing(dataRequestId, uid, bur, relationSince);
    });
  }

  private void runInRequestContext(UUID dataRequestId, String producer, Runnable work) {
    var requestContext = Arc.container().requestContext();
    requestContext.activate();
    try {
      identity.setRunAsUserId(USER_ID_LEGALLY_PERMITTED_CONSENT);
      work.run();
      log.debug("Created legally permitted consent request for dataRequestId={}, {}", dataRequestId, producer);
    } catch (Exception e) {
      if (isUniqueConstraintViolation(e)) {
        log.debug("Legally permitted consent request already exists for dataRequestId={}, {}", dataRequestId, producer);
      } else {
        log.error("Failed to create legally permitted consent request for dataRequestId={}, {}", dataRequestId, producer, e);
      }
    } finally {
      requestContext.deactivate();
    }
  }

  private boolean isUniqueConstraintViolation(Throwable e) {
    for (var cause = e; cause != null; cause = cause.getCause()) {
      if (cause instanceof org.hibernate.exception.ConstraintViolationException) {
        return true;
      }
    }
    return false;
  }

  /**
   * Test support: lets tests observe the outcome of the asynchronous consent creation deterministically, without polling or fixed waits.
   *
   * <p>Blocks until all tasks enqueued so far have been processed. As a single worker drains the queue in FIFO order, a marker task that
   * completes proves every earlier task (including its committed transaction) is done.
   */
  public void awaitAllProcessed() {
    try {
      executor.submit(() -> {
      }).get(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    } catch (ExecutionException | TimeoutException e) {
      throw new IllegalStateException(e);
    }
  }

  @PreDestroy
  void shutdown() {
    executor.shutdownNow();
  }
}
