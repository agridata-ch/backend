package ch.agridata.agreement.service;

import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.notification.api.NotificationApi;
import ch.agridata.notification.dto.EventTypeCodeEnum;
import ch.agridata.notification.dto.RecipientRequestDto;
import ch.agridata.notification.dto.TargetTypeCodeEnum;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.user.api.UserApi;
import ch.agridata.user.dto.UserNotificationInfoDto;
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

  @ConfigProperty(name = "agridata.notifications.frontend-url-paths.provider-path", defaultValue = "/data-requests-provider/{id}")
  String dataRequestProviderPath;

  @ConfigProperty(name = "agridata.notifications.frontend-url-paths.consumer-path", defaultValue = "/data-requests/{id}")
  String dataRequestConsumerPath;

  @ConfigProperty(name = "agridata.base-url")
  String baseUrl;

  private final NotificationApi notificationApi;
  private final UserApi userApi;
  private final DataProductApi dataProductApi;

  public void queueDataRequestInReview(DataRequestEntity request) {
    Map<String, String> placeholders = buildDataRequestPlaceholders(request, buildDataRequestUrl(dataRequestAdminPath, request.getId()));
    List<RecipientRequestDto> recipients = buildRecipients(userApi.getAdminsNotificationInfos());

    notificationApi.queueNotification(
        recipients,
        EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW,
        placeholders,
        TargetTypeCodeEnum.DATA_REQUEST,
        request.getId()
    );
  }

  public void queueDataRequestReadyForProviderSigning(DataRequestEntity request) {
    Map<String, String> placeholders = buildDataRequestPlaceholders(request, buildDataRequestUrl(dataRequestProviderPath, request.getId()));
    String providerUid = dataProductApi.getDataSourceSystem(request.getDataSourceSystemId()).dataProvider().uid();
    List<RecipientRequestDto> recipients = buildRecipients(userApi.getProvidersNotificationInfoByUid(providerUid));

    notificationApi.queueNotification(
        recipients,
        EventTypeCodeEnum.DATA_REQUEST_READY_FOR_PROVIDER_SIGNING,
        placeholders,
        TargetTypeCodeEnum.DATA_REQUEST,
        request.getId()
    );
  }

  public void queueDataRequestReadyForActivation(DataRequestEntity request) {
    // Notify consumer
    Map<String, String> consumerPlaceholders = buildDataRequestPlaceholders(
        request,
        buildDataRequestUrl(dataRequestConsumerPath, request.getId())
    );
    List<RecipientRequestDto> consumerRecipients = buildRecipients(userApi.getConsumersNotificationInfoByUid(request.getDataConsumerUid()));
    notificationApi.queueNotification(
        consumerRecipients,
        EventTypeCodeEnum.DATA_REQUEST_READY_FOR_ACTIVATION,
        consumerPlaceholders,
        TargetTypeCodeEnum.DATA_REQUEST,
        request.getId()
    );

    // Notify admins
    Map<String, String> adminPlaceholders = buildDataRequestPlaceholders(
        request,
        buildDataRequestUrl(dataRequestAdminPath, request.getId())
    );
    List<RecipientRequestDto> adminRecipients = buildRecipients(userApi.getAdminsNotificationInfos());
    notificationApi.queueNotification(
        adminRecipients,
        EventTypeCodeEnum.DATA_REQUEST_READY_FOR_ACTIVATION,
        adminPlaceholders,
        TargetTypeCodeEnum.DATA_REQUEST,
        request.getId()
    );
  }

  public void queueDataRequestChangesNeeded(DataRequestEntity request) {
    Map<String, String> placeholders = buildDataRequestPlaceholders(request, buildDataRequestUrl(dataRequestConsumerPath, request.getId()));
    List<RecipientRequestDto> recipients = buildRecipients(userApi.getConsumersNotificationInfoByUid(request.getDataConsumerUid()));

    notificationApi.queueNotification(
        recipients,
        EventTypeCodeEnum.DATA_REQUEST_CHANGES_NEEDED,
        placeholders,
        TargetTypeCodeEnum.DATA_REQUEST,
        request.getId()
    );
  }

  public void queueDataRequestApproved(DataRequestEntity request) {
    Map<String, String> placeholders = buildDataRequestPlaceholders(request, buildDataRequestUrl(dataRequestConsumerPath, request.getId()));
    List<RecipientRequestDto> recipients = buildRecipients(userApi.getConsumersNotificationInfoByUid(request.getDataConsumerUid()));

    notificationApi.queueNotification(
        recipients,
        EventTypeCodeEnum.DATA_REQUEST_APPROVED,
        placeholders,
        TargetTypeCodeEnum.DATA_REQUEST,
        request.getId()
    );
  }

  public void queueDataRequestActivated(DataRequestEntity request) {
    Map<String, String> placeholders = buildDataRequestPlaceholders(request, buildDataRequestUrl(dataRequestConsumerPath, request.getId()));
    List<RecipientRequestDto> recipients = buildRecipients(userApi.getConsumersNotificationInfoByUid(request.getDataConsumerUid()));

    notificationApi.queueNotification(
        recipients,
        EventTypeCodeEnum.DATA_REQUEST_ACTIVATED,
        placeholders,
        TargetTypeCodeEnum.DATA_REQUEST,
        request.getId()
    );
  }

  private @NonNull List<RecipientRequestDto> buildRecipients(List<UserNotificationInfoDto> userNotificationInfoDtoList) {
    return userNotificationInfoDtoList.stream()
        .map(userInfo -> new RecipientRequestDto(userInfo.userId(), userInfo.email(), userInfo.language()))
        .toList();
  }

  private String buildDataRequestUrl(String roleSpecificPath, UUID dataRequestId) {
    return baseUrl + roleSpecificPath.replace("{id}", dataRequestId.toString());
  }

  private @NonNull HashMap<String, String> buildDataRequestPlaceholders(DataRequestEntity request, String roleSpecificDataRequestUrl) {
    var placeholders = new HashMap<String, String>();
    placeholders.put("dataRequestTitleDe", request.getTitle().de());
    placeholders.put("dataRequestTitleFr", request.getTitle().fr());
    placeholders.put("dataRequestTitleIt", request.getTitle().it());
    placeholders.put("dataConsumer", request.getDataConsumerDisplayName());
    placeholders.put("dataRequestHumanFriendlyId", request.getHumanFriendlyId());
    placeholders.put("dataRequestUrl", roleSpecificDataRequestUrl);
    return placeholders;
  }
}
