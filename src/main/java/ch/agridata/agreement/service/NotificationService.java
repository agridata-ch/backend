package ch.agridata.agreement.service;

import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.notification.api.NotificationApi;
import ch.agridata.notification.dto.EventTypeCodeEnum;
import ch.agridata.notification.dto.RecipientRequestDto;
import ch.agridata.user.api.UserApi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Queues notifications for agreement-related events.
 *
 * @CommentLastReviewed 2026-05-08
 */
@ApplicationScoped
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationApi api;
  private final UserApi userApi;
  private final DataRequestRepository dataRequestRepository;

  public void queueDataRequestInReview(UUID requestId) {
    DataRequestEntity request = dataRequestRepository.findByIdOptional(requestId)
        .orElseThrow(() -> new NotFoundException("Data request not found: " + requestId));

    Map<String, String> placeholders = Map.of(
        "data_request_title_de", request.getTitle().de(),
        "data_request_title_fr", request.getTitle().fr(),
        "data_request_title_it", request.getTitle().it()
    );

    List<RecipientRequestDto> recipients = userApi.getAdminUserIds().stream().map(id -> new RecipientRequestDto(id, null)).toList();
    api.queueNotification(recipients, EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW, placeholders);
  }
}
