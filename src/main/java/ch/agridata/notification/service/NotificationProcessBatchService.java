package ch.agridata.notification.service;

import ch.agridata.notification.persistence.NotificationBatchEntity;
import ch.agridata.notification.persistence.NotificationBatchRepository;
import ch.agridata.notification.persistence.NotificationBatchStatusEnum;
import ch.agridata.notification.persistence.NotificationRecipientRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Processes pending notification batches from the outbox. Each batch is transitioned through
 * IN_PROGRESS → COMPLETE / PARTIALLY_FAILED / FAILED, while each individual recipient is
 * processed in its own independent transaction via {@link NotificationProcessRecipientService}.
 *
 * <p>Intended to be called exclusively by the scheduled outbox processor job.</p>
 *
 * @CommentLastReviewed 2026-04-29
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class NotificationProcessBatchService {

  private final NotificationBatchRepository batchRepository;
  private final NotificationRecipientRepository recipientRepository;
  private final NotificationProcessRecipientService processRecipientService;
  private final NotificationPlaceholderService placeholderService;

  /**
   * Loads all PENDING batches (with a row-level lock) and processes each one.
   * The batch status transitions are committed atomically when this transaction completes.
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public void processPendingBatches() {
    List<NotificationBatchEntity> pendingBatches = batchRepository.findPendingWithLock();

    if (pendingBatches.isEmpty()) {
      log.debug("No pending notification batches found.");
      return;
    }

    log.info("Processing {} pending notification batch(es).", pendingBatches.size());
    pendingBatches.forEach(this::processBatch);
  }

  private void processBatch(NotificationBatchEntity batch) {
    try {
      var resolvedNotificationTexts = placeholderService.resolve(batch);
      var recipients = recipientRepository.findByBatchId(batch.getId());
      int inboxCreated = 0;
      int emailSubmitted = 0;
      int emailSubmissionFailed = 0;

      for (var recipient : recipients) {
        var result = processRecipientService.processRecipient(recipient.getId(), resolvedNotificationTexts);
        if (result.inboxCreated()) {
          inboxCreated++;
        }
        if (result.emailSubmitted()) {
          emailSubmitted++;
        }
        if (result.emailSubmissionFailed()) {
          emailSubmissionFailed++;
        }
      }

      NotificationBatchStatusEnum finalStatus;
      if (emailSubmissionFailed == 0) {
        finalStatus = NotificationBatchStatusEnum.COMPLETE;
      } else if (emailSubmitted > 0) {
        finalStatus = NotificationBatchStatusEnum.PARTIALLY_FAILED;
      } else {
        finalStatus = NotificationBatchStatusEnum.FAILED;
      }
      batch.setStatusCode(finalStatus);

      log.info(
          "Processed batch {} – {} recipient(s), {} inbox entr(ies) created, {} email(s) submitted to AWS, {} email submission(s) failed.",
          batch.getId(),
          recipients.size(),
          inboxCreated,
          emailSubmitted,
          emailSubmissionFailed
      );
    } catch (Exception e) {
      log.error("Failed to process notification batch {}.", batch.getId(), e);
      batch.setStatusCode(NotificationBatchStatusEnum.FAILED);
      throw e;
    }
  }
}
