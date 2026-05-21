package ch.agridata.notification.service;

import ch.agridata.notification.dto.EventTypeCodeEnum;
import ch.agridata.notification.dto.RecipientRequestDto;
import ch.agridata.notification.persistence.NotificationBatchEntity;
import ch.agridata.notification.persistence.NotificationBatchRepository;
import ch.agridata.notification.persistence.NotificationBatchStatusEnum;
import ch.agridata.notification.persistence.NotificationRecipientEntity;
import ch.agridata.notification.persistence.NotificationRecipientRepository;
import ch.agridata.notification.persistence.NotificationTemplateEntity;
import ch.agridata.notification.persistence.NotificationTemplateRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Queues notification batches for asynchronous processing by the outbox processor job.
 * Creates one {@link NotificationBatchEntity} per call and one {@link NotificationRecipientEntity}
 * per recipient entry. After the surrounding transaction commits, a {@link NotificationBatchQueuedEvent}
 * is fired so that {@code NotificationQueueWorkerJob} can immediately pick the batch up.
 *
 * @CommentLastReviewed 2026-05-18
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class NotificationBatchService {

  private final NotificationTemplateRepository templateRepository;
  private final NotificationBatchRepository batchRepository;
  private final NotificationRecipientRepository recipientRepository;
  private final NotificationPlaceholderService placeholderService;
  private final Event<NotificationBatchQueuedEvent> batchQueuedEvent;

  /**
   * Creates a PENDING batch and individual recipient rows for the given event type and recipients.
   *
   * @throws NotFoundException        if no template exists for the given event type code
   * @throws IllegalArgumentException if any placeholder referenced by the template texts is missing from {@code placeholders}
   */
  @Transactional
  public void queueNotification(
      List<RecipientRequestDto> recipients,
      EventTypeCodeEnum eventTypeCode,
      Map<String, String> placeholders
  ) {
    var template = templateRepository.findLatestByEventTypeCode(eventTypeCode.name())
        .orElseThrow(() -> new NotFoundException("No template found for event type: " + eventTypeCode));

    validateRequiredPlaceholdersPresent(eventTypeCode, placeholders, template);

    var batch = NotificationBatchEntity.builder()
        .template(template)
        .placeholders(placeholders)
        .statusCode(NotificationBatchStatusEnum.PENDING)
        .build();
    batchRepository.persist(batch);

    for (RecipientRequestDto recipientDto : recipients) {
      var recipient = NotificationRecipientEntity.builder()
          .batch(batch)
          .userId(recipientDto.userId())
          .email(recipientDto.email())
          .build();
      recipientRepository.persist(recipient);
    }

    log.info("Queued notification batch for event type '{}' with {} recipient(s).", eventTypeCode, recipients.size());

    batchQueuedEvent.fire(new NotificationBatchQueuedEvent());
  }

  private void validateRequiredPlaceholdersPresent(
      EventTypeCodeEnum eventTypeCode,
      Map<String, String> placeholders,
      NotificationTemplateEntity template
  ) {
    var missing = placeholderService.extractRequiredPlaceholders(template).stream()
        .filter(key -> !placeholders.containsKey(key))
        .toList();
    if (!missing.isEmpty()) {
      throw new IllegalArgumentException("Missing required placeholders for event type " + eventTypeCode + ": " + missing);
    }
  }
}
