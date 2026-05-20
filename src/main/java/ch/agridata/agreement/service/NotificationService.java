package ch.agridata.agreement.service;

import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.notification.api.NotificationApi;
import ch.agridata.notification.dto.EventTypeCodeEnum;
import ch.agridata.notification.dto.RecipientRequestDto;
import ch.agridata.notification.dto.TargetTypeCodeEnum;
import ch.agridata.user.api.UserApi;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Queues notifications for agreement-related events.
 *
 * @CommentLastReviewed 2026-05-18
 */
@ApplicationScoped
@RequiredArgsConstructor
public class NotificationService {

  public static final String DATA_REQUEST_ADMIN_PATH = "/admin/%s";

  @ConfigProperty(name = "agridata.base-url")
  String baseUrl;

  private final NotificationApi api;
  private final UserApi userApi;

  public void queueDataRequestInReview(DataRequestEntity request) {
    Map<String, String> placeholders = Map.of(
        "dataRequestUrl", buildDataRequestAdminUrl(request.getId()),
        "dataRequestTitleDe", request.getTitle().de(),
        "dataRequestTitleFr", request.getTitle().fr(),
        "dataRequestTitleIt", request.getTitle().it(),
        "dataConsumer", request.getDataConsumerDisplayName()
    );

    List<RecipientRequestDto> recipients = userApi.getAdminUsers()
        .stream()
        .map(admin -> new RecipientRequestDto(admin.userId(), admin.email()))
        .toList();
    api.queueNotification(recipients, EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW, placeholders, TargetTypeCodeEnum.DATA_REQUEST,
        request.getId());
  }

  private String buildDataRequestAdminUrl(UUID id) {
    return baseUrl + String.format(DATA_REQUEST_ADMIN_PATH, id);
  }
}
