package ch.agridata.notification.service;

import static org.mockito.Mockito.verify;

import ch.agridata.notification.dto.EventTypeCodeEnum;
import ch.agridata.notification.dto.RecipientRequestDto;
import ch.agridata.notification.dto.TargetTypeCodeEnum;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link NotificationApiImpl}.
 *
 * @CommentLastReviewed 2026-05-06
 */
@ExtendWith(MockitoExtension.class)
class NotificationApiImplTest {

  @InjectMocks
  private NotificationApiImpl service;

  @Mock
  private NotificationBatchService batchService;

  @Test
  void givenValidRequest_whenQueueNotification_thenDelegatesToBatchService() {
    var recipients = List.of(new RecipientRequestDto(UUID.randomUUID(), null, null));
    var eventTypeCode = EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW;
    var placeholders = Map.of("requestTitle", "Test");
    var targetTypeCode = TargetTypeCodeEnum.DATA_REQUEST;
    var targetId = UUID.randomUUID();

    service.queueNotification(recipients, eventTypeCode, placeholders, targetTypeCode, targetId);

    verify(batchService).queueNotification(recipients, eventTypeCode, placeholders, targetTypeCode, targetId);
  }
}
