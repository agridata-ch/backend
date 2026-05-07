package ch.agridata.notification.service;

import ch.agridata.notification.dto.EventTypeCodeEnum;
import ch.agridata.notification.dto.RecipientRequestDto;
import ch.agridata.notification.persistence.NotificationBatchEntity;
import ch.agridata.notification.persistence.NotificationBatchRepository;
import ch.agridata.notification.persistence.NotificationBatchStatusEnum;
import ch.agridata.notification.persistence.NotificationRecipientEntity;
import ch.agridata.notification.persistence.NotificationRecipientRepository;
import ch.agridata.notification.persistence.NotificationTemplateRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Queues notification batches for asynchronous processing by the outbox processor job.
 * Creates one {@link NotificationBatchEntity} per call and one {@link NotificationRecipientEntity}
 * per recipient entry.
 *
 * @CommentLastReviewed 2026-04-22
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class NotificationBatchService {

  private final NotificationTemplateRepository templateRepository;
  private final NotificationBatchRepository batchRepository;
  private final NotificationRecipientRepository recipientRepository;

  /**
   * Creates a PENDING batch and individual recipient rows for the given event type and recipients.
   *
   * @throws NotFoundException if no template exists for the given event type code
   */
  @Transactional
  public void queueNotification(
      List<RecipientRequestDto> recipients, EventTypeCodeEnum eventTypeCode,
      Map<String, String> placeholders
  ) {
    var template = templateRepository.findLatestByEventTypeCode(eventTypeCode.name())
        .orElseThrow(() -> new NotFoundException("No template found for event type: " + eventTypeCode));

    var batch = NotificationBatchEntity.builder()
        .template(template)
        .placeholders(placeholders)
        .statusCode(NotificationBatchStatusEnum.PENDING)
        .build();
    batchRepository.persist(batch);

    for (RecipientRequestDto r : recipients) {
      var recipient = NotificationRecipientEntity.builder()
          .batch(batch)
          .userId(r.userId())
          .email(r.email())
          .build();
      recipientRepository.persist(recipient);
    }

    log.info("Queued notification batch for event type '{}' with {} recipient(s).", eventTypeCode, recipients.size());
  }
}
