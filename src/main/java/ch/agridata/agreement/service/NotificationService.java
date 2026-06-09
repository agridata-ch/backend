package ch.agridata.agreement.service;

import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.notification.api.NotificationApi;
import ch.agridata.notification.dto.EventTypeCodeEnum;
import ch.agridata.notification.dto.RecipientRequestDto;
import ch.agridata.notification.dto.TargetTypeCodeEnum;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.user.api.UserApi;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jspecify.annotations.NonNull;

/**
 * Queues notifications for agreement-related events.
 *
 * @CommentLastReviewed 2026-05-18
 */
@ApplicationScoped
@RequiredArgsConstructor
public class NotificationService {

  @ConfigProperty(name = "agridata.notifications.frontend-url-paths.admin-path", defaultValue = "/admin/{id}")
  String dataRequestAdminPath;

  @ConfigProperty(name = "agridata.notifications.frontend-url-paths.provider-path", defaultValue = "/provider/{id}")
  String dataRequestProviderPath;

  @ConfigProperty(name = "agridata.base-url")
  String baseUrl;

  private final NotificationApi notificationApi;
  private final UserApi userApi;
  private final DataProductApi dataProductApi;

  public void queueDataRequestInReview(DataRequestEntity request) {
    Map<String, String> placeholders = buildDataRequestPlaceholders(request);
    placeholders.put("dataRequestUrl", buildDataRequestAdminUrl(request.getId()));

    List<RecipientRequestDto> recipients = userApi.getAdminUsers()
        .stream()
        .map(userInfo -> new RecipientRequestDto(
            userInfo.userId(),
            userInfo.email(),
            userInfo.language()
        ))
        .toList();
    notificationApi.queueNotification(
        recipients,
        EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW,
        placeholders,
        TargetTypeCodeEnum.DATA_REQUEST,
        request.getId()
    );
  }

  public void queueDataRequestReadyForProviderSigning(DataRequestEntity request) {
    Map<String, String> placeholders = buildDataRequestPlaceholders(request);
    placeholders.put("dataRequestUrl", buildDataRequestProviderUrl(request.getId()));

    String providerUid = dataProductApi.getDataSourceSystem(request.getDataSourceSystemId()).dataProvider().uid();

    List<RecipientRequestDto> recipients = userApi.getProviderUsers(providerUid)
        .stream()
        .map(userInfo -> new RecipientRequestDto(
            userInfo.userId(),
            userInfo.email(),
            userInfo.language()
        ))
        .toList();
    notificationApi.queueNotification(
        recipients,
        EventTypeCodeEnum.DATA_REQUEST_READY_FOR_PROVIDER_SIGNING,
        placeholders,
        TargetTypeCodeEnum.DATA_REQUEST,
        request.getId()
    );
  }

  private String buildDataRequestAdminUrl(UUID id) {
    return baseUrl + dataRequestAdminPath.replace("{id}", id.toString());
  }

  private String buildDataRequestProviderUrl(UUID id) {
    return baseUrl + dataRequestProviderPath.replace("{id}", id.toString());
  }

  private @NonNull HashMap<String, String> buildDataRequestPlaceholders(DataRequestEntity request) {
    var placeholders = new HashMap<String, String>();
    placeholders.put("dataRequestTitleDe", request.getTitle().de());
    placeholders.put("dataRequestTitleFr", request.getTitle().fr());
    placeholders.put("dataRequestTitleIt", request.getTitle().it());
    placeholders.put("dataConsumer", request.getDataConsumerDisplayName());
    placeholders.put("dataRequestHumanFriendlyId", request.getHumanFriendlyId());
    return placeholders;
  }
}
