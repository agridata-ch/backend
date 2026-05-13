package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.common.persistence.TranslationPersistenceDto;
import ch.agridata.notification.api.NotificationApi;
import ch.agridata.notification.dto.EventTypeCodeEnum;
import ch.agridata.notification.dto.RecipientRequestDto;
import ch.agridata.user.api.UserApi;
import ch.agridata.user.dto.AdminUserDto;
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

  @BeforeEach
  void setUp() {
    service.baseUrl = "https://agridata.ch";
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private static DataRequestEntity entityWithId(UUID id) {
    return DataRequestEntity.builder()
        .id(id)
        .title(new TranslationPersistenceDto("Titel DE", "Titre FR", "Titolo IT"))
        .dataConsumerDisplayName("Bio Suisse")
        .build();
  }

  // ── queueDataRequestInReview ──────────────────────────────────────────────

  @Test
  void givenMultipleAdmins_whenQueueDataRequestInReview_thenQueuesNotificationForEachAdminWithAllPlaceholders() {
    var admin1 = new AdminUserDto(UUID.randomUUID(), "admin1@example.com");
    var admin2 = new AdminUserDto(UUID.randomUUID(), null);
    when(userApi.getAdminUsers()).thenReturn(List.of(admin1, admin2));

    var id = UUID.randomUUID();
    service.queueDataRequestInReview(entityWithId(id));

    @SuppressWarnings("unchecked") ArgumentCaptor<List<RecipientRequestDto>> recipientCaptor = ArgumentCaptor.forClass(List.class);
    @SuppressWarnings("unchecked") ArgumentCaptor<Map<String, String>> placeholderCaptor = ArgumentCaptor.forClass(Map.class);
    verify(api).queueNotification(
        recipientCaptor.capture(),
        eq(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW),
        placeholderCaptor.capture()
    );

    assertThat(recipientCaptor.getValue()).hasSize(2)
        .extracting(RecipientRequestDto::userId)
        .containsExactly(admin1.userId(), admin2.userId());
    assertThat(recipientCaptor.getValue()).extracting(RecipientRequestDto::email)
        .containsExactly("admin1@example.com", null);

    assertThat(placeholderCaptor.getValue())
        .containsEntry("dataRequestTitleDe", "Titel DE")
        .containsEntry("dataRequestTitleFr", "Titre FR")
        .containsEntry("dataRequestTitleIt", "Titolo IT")
        .containsEntry("dataConsumer", "Bio Suisse")
        .containsEntry("dataRequestUrl", "https://agridata.ch/admin/" + id);
  }

  @Test
  void givenNoAdmins_whenQueueDataRequestInReview_thenQueuesNotificationWithEmptyRecipients() {
    when(userApi.getAdminUsers()).thenReturn(List.of());

    service.queueDataRequestInReview(entityWithId(UUID.randomUUID()));

    @SuppressWarnings("unchecked") ArgumentCaptor<Map<String, String>> placeholderCaptor = ArgumentCaptor.forClass(Map.class);
    verify(api).queueNotification(
        eq(List.of()),
        eq(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW),
        placeholderCaptor.capture()
    );
    assertThat(placeholderCaptor.getValue())
        .containsEntry("dataRequestTitleDe", "Titel DE")
        .containsEntry("dataRequestTitleFr", "Titre FR")
        .containsEntry("dataRequestTitleIt", "Titolo IT")
        .containsEntry("dataConsumer", "Bio Suisse");
  }
}
