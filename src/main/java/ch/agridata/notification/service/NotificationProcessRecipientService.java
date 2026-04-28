package ch.agridata.notification.service;

import ch.agridata.notification.persistence.NotificationDispatchRepository;
import ch.agridata.notification.persistence.NotificationInboxEntity;
import ch.agridata.notification.persistence.NotificationInboxRepository;
import ch.agridata.notification.persistence.NotificationRecipientEntity;
import ch.agridata.notification.persistence.NotificationRecipientRepository;
import ch.agridata.notification.persistence.NotificationTemplateEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Processes a single notification recipient within its own independent transaction
 * ({@link Transactional.TxType#REQUIRES_NEW}), so each recipient's inbox creation and email
 * dispatch are committed to the database before the next recipient is processed.
 *
 * <p>Every operation is guarded by an idempotency check: if a recipient was already processed
 * in a previous (partially committed) run, it is silently skipped and no duplicate inbox entry
 * or email is produced.
 *
 * @CommentLastReviewed 2026-04-28
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class NotificationProcessRecipientService {

  private final NotificationRecipientRepository recipientRepository;
  private final NotificationInboxRepository inboxRepository;
  private final NotificationDispatchRepository dispatchRepository;
  private final NotificationSubmitEmailService emailDispatchService;

  /**
   * Processes a single recipient: creates an inbox entry if the recipient has a user ID and one
   * does not yet exist, and dispatches an email if the recipient has an email address and no
   * successful dispatch record yet exists. Both operations are committed atomically.
   *
   * @param recipientId         the ID of the recipient to process
   * @param template            the pre-loaded notification template (read-only; fields must be
   *                            initialized in the caller's transaction before this is invoked)
   * @param genericPlaceholders placeholder values to substitute into the template
   * @return the processing outcome for this recipient
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public RecipientProcessingResult processRecipient(
      UUID recipientId,
      NotificationTemplateEntity template,
      Map<String, String> genericPlaceholders
  ) {

    var recipient = recipientRepository.findByIdOptional(recipientId)
        .orElseThrow(() -> new IllegalStateException("Recipient not found: " + recipientId));

    boolean inboxCreated = false;
    if (recipient.getUserId() != null && !inboxRepository.existsByRecipientId(recipientId)) {
      inboxRepository.persist(createInboxEntry(recipient));
      inboxCreated = true;
      log.debug("Created inbox entry for recipient {}.", recipientId);
    }

    boolean emailSent = false;
    boolean emailFailed = false;
    if (recipient.getEmail() != null && !dispatchRepository.existsSentByRecipientId(recipientId)) {
      boolean success = emailDispatchService.dispatch(recipient, template, genericPlaceholders);
      if (success) {
        emailSent = true;
      } else {
        emailFailed = true;
      }
    }

    return new RecipientProcessingResult(inboxCreated, emailSent, emailFailed);
  }

  private static NotificationInboxEntity createInboxEntry(NotificationRecipientEntity recipient) {
    return NotificationInboxEntity.builder().recipient(recipient).userId(recipient.getUserId()).isRead(false).build();
  }

  /**
   * Holds the outcome of processing a single notification recipient.
   *
   * @param inboxCreated          whether a new inbox entry was created for this recipient in this run
   * @param emailSubmitted        whether an email was successfully handed over to AWS SES
   * @param emailSubmissionFailed whether an email submission attempt was made but failed or rejected by AWS SES
   * @CommentLastReviewed 2026-04-28
   */
  public record RecipientProcessingResult(boolean inboxCreated, boolean emailSubmitted, boolean emailSubmissionFailed) {}
}
