package ch.agridata.agreement.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.persistence.TranslationPersistenceDto;
import ch.agridata.notification.api.NotificationApi;
import ch.agridata.notification.dto.EventTypeCodeEnum;
import ch.agridata.notification.dto.RecipientRequestDto;
import ch.agridata.user.api.UserApi;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link NotificationService}.
 *
 * @CommentLastReviewed 2026-05-08
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
  private DataRequestRepository dataRequestRepository;

  @Test
  void givenMultipleAdmins_whenQueueDataRequestInReview_thenQueuesNotificationForEachAdminWithTitlePlaceholders() {
    var requestId = UUID.randomUUID();
    var adminId1 = UUID.randomUUID();
    var adminId2 = UUID.randomUUID();
    var request = new DataRequestEntity();
    request.setTitle(new TranslationPersistenceDto("Titel DE", "Titre FR", "Titolo IT"));
    when(dataRequestRepository.findByIdOptional(requestId)).thenReturn(Optional.of(request));
    when(userApi.getAdminUserIds()).thenReturn(List.of(adminId1, adminId2));

    service.queueDataRequestInReview(requestId);

    verify(api).queueNotification(
        List.of(new RecipientRequestDto(adminId1, null), new RecipientRequestDto(adminId2, null)),
        EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW,
        Map.of(
            "data_request_title_de", "Titel DE",
            "data_request_title_fr", "Titre FR",
            "data_request_title_it", "Titolo IT"
        )
    );
  }

  @Test
  void givenNoAdmins_whenQueueDataRequestInReview_thenQueuesNotificationWithEmptyRecipients() {
    var requestId = UUID.randomUUID();
    var request = new DataRequestEntity();
    request.setTitle(new TranslationPersistenceDto("Titel DE", "Titre FR", "Titolo IT"));
    when(dataRequestRepository.findByIdOptional(requestId)).thenReturn(Optional.of(request));
    when(userApi.getAdminUserIds()).thenReturn(List.of());

    service.queueDataRequestInReview(requestId);

    verify(api).queueNotification(
        List.of(),
        EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW,
        Map.of(
            "data_request_title_de", "Titel DE",
            "data_request_title_fr", "Titre FR",
            "data_request_title_it", "Titolo IT"
        )
    );
  }
}