package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.common.dto.SupportedLanguage;
import ch.agridata.common.persistence.TranslationPersistenceDto;
import ch.agridata.notification.api.NotificationApi;
import ch.agridata.notification.dto.EventTypeCodeEnum;
import ch.agridata.notification.dto.RecipientRequestDto;
import ch.agridata.notification.dto.TargetTypeCodeEnum;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.product.dto.DataProviderDto;
import ch.agridata.product.dto.DataSourceSystemDto;
import ch.agridata.user.api.UserApi;
import ch.agridata.user.dto.UserNotificationInfoDto;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link NotificationService}.
 *
 * @CommentLastReviewed 2026-05-18
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @InjectMocks
  private NotificationService service;

  @Mock
  private NotificationApi api;

  @Mock
  private UserApi userApi;

  @Mock
  private DataProductApi dataProductApi;

  @BeforeEach
  void setUp() {
    service.baseUrl = "https://agridata.ch";
    service.dataRequestAdminPath = "/admin/{id}";
    service.dataRequestProviderPath = "/provider/{id}";
    service.dataRequestConsumerPath = "/consumer/{id}";
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private static DataRequestEntity entityWithId(UUID id) {
    return DataRequestEntity.builder()
        .id(id)
        .title(new TranslationPersistenceDto("Titel DE", "Titre FR", "Titolo IT"))
        .dataConsumerDisplayName("Bio Suisse")
        .dataConsumerUid("CHE123456789")
        .build();
  }

  // ── queueDataRequestInReview ──────────────────────────────────────────────

  @Test
  void givenMultipleAdmins_whenQueueDataRequestInReview_thenQueuesNotificationForEachAdminWithAllPlaceholders() {
    var admin1 = new UserNotificationInfoDto(UUID.randomUUID(), "admin1@example.com", SupportedLanguage.DE);
    var admin2 = new UserNotificationInfoDto(UUID.randomUUID(), null, null);
    when(userApi.getAdminsNotificationInfos()).thenReturn(List.of(admin1, admin2));

    var id = UUID.randomUUID();
    service.queueDataRequestInReview(entityWithId(id));

    @SuppressWarnings("unchecked") ArgumentCaptor<List<RecipientRequestDto>> recipientCaptor = ArgumentCaptor.forClass(List.class);
    @SuppressWarnings("unchecked") ArgumentCaptor<Map<String, String>> placeholderCaptor = ArgumentCaptor.forClass(Map.class);
    verify(api).queueNotification(
        recipientCaptor.capture(),
        eq(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW),
        placeholderCaptor.capture(),
        eq(TargetTypeCodeEnum.DATA_REQUEST),
        eq(id)
    );

    assertThat(recipientCaptor.getValue()).hasSize(2)
        .extracting(RecipientRequestDto::userId)
        .containsExactly(admin1.userId(), admin2.userId());
    assertThat(recipientCaptor.getValue()).extracting(RecipientRequestDto::email)
        .containsExactly("admin1@example.com", null);
    assertThat(recipientCaptor.getValue()).extracting(RecipientRequestDto::language)
        .containsExactly(SupportedLanguage.DE, null);

    assertThat(placeholderCaptor.getValue())
        .containsEntry("dataRequestTitleDe", "Titel DE")
        .containsEntry("dataRequestTitleFr", "Titre FR")
        .containsEntry("dataRequestTitleIt", "Titolo IT")
        .containsEntry("dataConsumer", "Bio Suisse")
        .containsEntry("dataRequestUrl", "https://agridata.ch/admin/" + id);
  }

  @Test
  void givenNoAdmins_whenQueueDataRequestInReview_thenQueuesNotificationWithEmptyRecipients() {
    when(userApi.getAdminsNotificationInfos()).thenReturn(List.of());
    var requestId = UUID.randomUUID();

    service.queueDataRequestInReview(entityWithId(requestId));

    @SuppressWarnings("unchecked") ArgumentCaptor<Map<String, String>> placeholderCaptor = ArgumentCaptor.forClass(Map.class);
    verify(api).queueNotification(
        eq(List.of()),
        eq(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW),
        placeholderCaptor.capture(),
        eq(TargetTypeCodeEnum.DATA_REQUEST),
        eq(requestId)

    );
    assertThat(placeholderCaptor.getValue())
        .containsEntry("dataRequestTitleDe", "Titel DE")
        .containsEntry("dataRequestTitleFr", "Titre FR")
        .containsEntry("dataRequestTitleIt", "Titolo IT")
        .containsEntry("dataConsumer", "Bio Suisse");
  }

  // ── queueDataRequestToBeSignedByProvider ──────────────────────────────────

  @Test
  void givenMultipleProviders_whenQueueDataRequestReadyForProvider_thenQueuesNotificationForEachWithAllPlaceholders() {
    var sourceSystemId = UUID.randomUUID();
    when(dataProductApi.getDataSourceSystem(sourceSystemId)).thenReturn(dataSourceSystemWithProviderUid("CHE123456789"));

    var provider1 = new UserNotificationInfoDto(UUID.randomUUID(), "provider1@example.com", SupportedLanguage.FR);
    var provider2 = new UserNotificationInfoDto(UUID.randomUUID(), null, null);
    when(userApi.getProvidersNotificationInfoByUid("CHE123456789")).thenReturn(List.of(provider1, provider2));

    var id = UUID.randomUUID();
    var entity = entityWithId(id);
    entity.setDataSourceSystemId(sourceSystemId);
    service.queueDataRequestReadyForProviderSigning(entity);

    @SuppressWarnings("unchecked") ArgumentCaptor<List<RecipientRequestDto>> recipientCaptor = ArgumentCaptor.forClass(List.class);
    @SuppressWarnings("unchecked") ArgumentCaptor<Map<String, String>> placeholderCaptor = ArgumentCaptor.forClass(Map.class);
    verify(api).queueNotification(
        recipientCaptor.capture(),
        eq(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_PROVIDER_SIGNING),
        placeholderCaptor.capture(),
        eq(TargetTypeCodeEnum.DATA_REQUEST),
        eq(id)
    );

    assertThat(recipientCaptor.getValue()).hasSize(2)
        .extracting(RecipientRequestDto::userId)
        .containsExactly(provider1.userId(), provider2.userId());
    assertThat(recipientCaptor.getValue()).extracting(RecipientRequestDto::language)
        .containsExactly(SupportedLanguage.FR, null);

    assertThat(placeholderCaptor.getValue())
        .containsEntry("dataRequestTitleDe", "Titel DE")
        .containsEntry("dataRequestTitleFr", "Titre FR")
        .containsEntry("dataRequestTitleIt", "Titolo IT")
        .containsEntry("dataConsumer", "Bio Suisse")
        .containsEntry("dataRequestUrl", "https://agridata.ch/provider/" + id);
  }

  private static DataSourceSystemDto dataSourceSystemWithProviderUid(String uid) {
    return DataSourceSystemDto.builder()
        .id(UUID.randomUUID())
        .dataProvider(DataProviderDto.builder().id(UUID.randomUUID()).uid(uid).build())
        .build();
  }

  // ── queueDataRequestReadyForActivation ────────────────────────────────────

  @Test
  void givenConsumerAndAdminUsers_whenQueueReadyForActivation_thenQueuesNotificationForConsumersWithConsumerUrlAndAdminsWithAdminUrl() {
    var consumer1 = new UserNotificationInfoDto(UUID.randomUUID(), "consumer1@example.com", SupportedLanguage.DE);
    when(userApi.getConsumersNotificationInfoByUid("CHE123456789")).thenReturn(List.of(consumer1));
    var admin1 = new UserNotificationInfoDto(UUID.randomUUID(), "admin1@example.com", SupportedLanguage.FR);
    when(userApi.getAdminsNotificationInfos()).thenReturn(List.of(admin1));

    var id = UUID.randomUUID();
    service.queueDataRequestReadyForActivation(entityWithId(id));

    @SuppressWarnings("unchecked") ArgumentCaptor<List<RecipientRequestDto>> recipientCaptor = ArgumentCaptor.forClass(List.class);
    @SuppressWarnings("unchecked") ArgumentCaptor<Map<String, String>> placeholderCaptor = ArgumentCaptor.forClass(Map.class);
    verify(api, org.mockito.Mockito.times(2)).queueNotification(
        recipientCaptor.capture(),
        eq(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_ACTIVATION),
        placeholderCaptor.capture(),
        eq(TargetTypeCodeEnum.DATA_REQUEST),
        eq(id)
    );

    var consumerRecipients = recipientCaptor.getAllValues().getFirst();
    assertThat(consumerRecipients).hasSize(1)
        .extracting(RecipientRequestDto::userId)
        .containsExactly(consumer1.userId());
    assertThat(placeholderCaptor.getAllValues().getFirst())
        .containsEntry("dataRequestUrl", "https://agridata.ch/consumer/" + id);

    var adminRecipients = recipientCaptor.getAllValues().get(1);
    assertThat(adminRecipients).hasSize(1)
        .extracting(RecipientRequestDto::userId)
        .containsExactly(admin1.userId());
    assertThat(placeholderCaptor.getAllValues().get(1))
        .containsEntry("dataRequestUrl", "https://agridata.ch/admin/" + id);
  }

  @Test
  void givenNoConsumerUsers_whenQueueReadyForActivation_thenQueuesNotificationWithEmptyConsumerRecipients() {
    when(userApi.getConsumersNotificationInfoByUid("CHE123456789")).thenReturn(List.of());
    when(userApi.getAdminsNotificationInfos()).thenReturn(List.of());

    var id = UUID.randomUUID();
    service.queueDataRequestReadyForActivation(entityWithId(id));

    @SuppressWarnings("unchecked") ArgumentCaptor<List<RecipientRequestDto>> recipientCaptor = ArgumentCaptor.forClass(List.class);
    verify(api, org.mockito.Mockito.times(2)).queueNotification(
        recipientCaptor.capture(),
        eq(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_ACTIVATION),
        org.mockito.ArgumentMatchers.any(),
        eq(TargetTypeCodeEnum.DATA_REQUEST),
        eq(id)
    );

    assertThat(recipientCaptor.getAllValues().getFirst()).isEmpty();
  }

  @Test
  void givenNoAdminUsers_whenQueueReadyForActivation_thenQueuesNotificationWithEmptyAdminRecipients() {
    var consumer1 = new UserNotificationInfoDto(UUID.randomUUID(), "consumer1@example.com", SupportedLanguage.DE);
    when(userApi.getConsumersNotificationInfoByUid("CHE123456789")).thenReturn(List.of(consumer1));
    when(userApi.getAdminsNotificationInfos()).thenReturn(List.of());

    var id = UUID.randomUUID();
    service.queueDataRequestReadyForActivation(entityWithId(id));

    @SuppressWarnings("unchecked") ArgumentCaptor<List<RecipientRequestDto>> recipientCaptor = ArgumentCaptor.forClass(List.class);
    verify(api, org.mockito.Mockito.times(2)).queueNotification(
        recipientCaptor.capture(),
        eq(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_ACTIVATION),
        org.mockito.ArgumentMatchers.any(),
        eq(TargetTypeCodeEnum.DATA_REQUEST),
        eq(id)
    );

    assertThat(recipientCaptor.getAllValues().get(1)).isEmpty();
  }

  // ── queueDataRequestActivated ─────────────────────────────────────────────

  @Test
  void givenMultipleConsumers_whenQueueDataRequestActivated_thenQueuesNotificationWithConsumerUrl() {
    var consumer1 = new UserNotificationInfoDto(UUID.randomUUID(), "consumer1@example.com", SupportedLanguage.DE);
    when(userApi.getConsumersNotificationInfoByUid("CHE123456789")).thenReturn(List.of(consumer1));

    var id = UUID.randomUUID();
    service.queueDataRequestActivated(entityWithId(id));

    @SuppressWarnings("unchecked") ArgumentCaptor<List<RecipientRequestDto>> recipientCaptor = ArgumentCaptor.forClass(List.class);
    @SuppressWarnings("unchecked") ArgumentCaptor<Map<String, String>> placeholderCaptor = ArgumentCaptor.forClass(Map.class);
    verify(api).queueNotification(
        recipientCaptor.capture(),
        eq(EventTypeCodeEnum.DATA_REQUEST_ACTIVATED),
        placeholderCaptor.capture(),
        eq(TargetTypeCodeEnum.DATA_REQUEST),
        eq(id)
    );

    assertThat(recipientCaptor.getValue()).hasSize(1)
        .extracting(RecipientRequestDto::userId)
        .containsExactly(consumer1.userId());
    assertThat(placeholderCaptor.getValue())
        .containsEntry("dataRequestTitleDe", "Titel DE")
        .containsEntry("dataRequestTitleFr", "Titre FR")
        .containsEntry("dataRequestTitleIt", "Titolo IT")
        .containsEntry("dataConsumer", "Bio Suisse")
        .containsEntry("dataRequestUrl", "https://agridata.ch/consumer/" + id);
  }

  @Test
  void givenNoConsumerUsers_whenQueueDataRequestActivated_thenQueuesNotificationWithEmptyRecipients() {
    when(userApi.getConsumersNotificationInfoByUid("CHE123456789")).thenReturn(List.of());

    var id = UUID.randomUUID();
    service.queueDataRequestActivated(entityWithId(id));

    verify(api).queueNotification(
        eq(List.of()),
        eq(EventTypeCodeEnum.DATA_REQUEST_ACTIVATED),
        org.mockito.ArgumentMatchers.any(),
        eq(TargetTypeCodeEnum.DATA_REQUEST),
        eq(id)
    );
  }
}
