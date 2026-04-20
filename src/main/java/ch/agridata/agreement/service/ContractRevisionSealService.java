package ch.agridata.agreement.service;

import ch.agridata.agreement.dto.SealAttemptStateEnum;
import ch.agridata.agreement.mapper.ContractRevisionMapper;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.agreement.persistence.ContractRevisionEntity.SealAttemptState;
import ch.agridata.agreement.persistence.ContractRevisionRepository;
import ch.agridata.bit.api.BitSignatureApi;
import ch.agridata.common.security.AgridataSecurityIdentity;
import io.quarkus.arc.Arc;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
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
  private final ContractRevisionMapper contractRevisionMapper;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final ManagedExecutor managedExecutor;
  private final ContractRevisionStorageService contractRevisionStorageService;

  public SealAttemptStateEnum getSealState(UUID contractRevisionId, boolean longPolling) {
    long deadline = System.currentTimeMillis() + 10_000;
    while (true) {
      SealAttemptStateEnum state = QuarkusTransaction.requiringNew().call(() ->
          contractRevisionRepository.findByIdOptional(contractRevisionId)
              .map(ContractRevisionEntity::getSealState)
              .map(contractRevisionMapper::toSealAttemptStateEnum)
              .orElseThrow(NotFoundException::new));
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
      byte[] pdf = createDummyPdf(contractRevisionId); // TODO: Replace the stub with the actual contract revision PDF.
      bitSignatureApi.sign(pdf, adminGlobalId);
      contractRevisionStorageService.upload(contractRevisionId, pdf);
      updateSealState(contractRevisionId, SealAttemptState.COMPLETED);
    } catch (Exception e) {
      log.error("BIT seal failed for contractRevisionId={}: {}", contractRevisionId, e.getMessage());
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

  private byte[] createDummyPdf(UUID contractRevisionId) {
    try (var doc = new PDDocument();
         var baos = new ByteArrayOutputStream()) {
      var page = new PDPage();
      doc.addPage(page);
      try (var cs = new PDPageContentStream(doc, page)) {
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12f);
        cs.newLineAtOffset(100f, 700f);
        cs.showText("Dummy PDF for Contract Revision " + contractRevisionId);
        cs.endText();
      }
      doc.save(baos);
      return baos.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create dummy PDF", e);
    }
  }
}
