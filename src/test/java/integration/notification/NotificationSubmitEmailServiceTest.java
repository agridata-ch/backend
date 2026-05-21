package integration.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.aws.api.EmailApi;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.common.persistence.TranslationPersistenceDto;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.notification.persistence.NotificationBatchEntity;
import ch.agridata.notification.persistence.NotificationBatchStatusEnum;
import ch.agridata.notification.persistence.NotificationDispatchEntity;
import ch.agridata.notification.persistence.NotificationDispatchRepository;
import ch.agridata.notification.persistence.NotificationDispatchStatusEnum;
import ch.agridata.notification.persistence.NotificationRecipientEntity;
import ch.agridata.notification.persistence.NotificationTemplateEntity;
import ch.agridata.notification.service.NotificationPlaceholderService;
import ch.agridata.notification.service.NotificationSubmitEmailService;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link NotificationSubmitEmailService}.
 * Verifies that dispatch entries are correctly persisted in the database
 * for both successful and failed email submissions.
 *
 * @CommentLastReviewed 2026-05-08
 */
@QuarkusTest
@RequiredArgsConstructor
class NotificationSubmitEmailServiceTest {

  private final NotificationSubmitEmailService service;
  private final NotificationPlaceholderService placeholderService;
  private final NotificationDispatchRepository dispatchRepository;
  private final EntityManager entityManager;

  @InjectMock
  EmailApi emailApi;

  @InjectMock
  AgridataSecurityIdentity securityIdentity;

  private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private UUID recipientId;
  private UUID batchId;

  @BeforeEach
  void setUpTestData() {
    when(securityIdentity.getUserId()).thenReturn(TEST_USER_ID);
    QuarkusTransaction.requiringNew().run(() -> {
      var template = NotificationTemplateEntity.builder()
          .eventTypeCode("TEST_EVENT")
          .templateVersion(1)
          .emailSubject(new TranslationPersistenceDto("Hello {{name}}", null, null))
          .emailText(new TranslationPersistenceDto("<p>Dear {{name}}</p>", null, null))
          .build();
      entityManager.persist(template);

      var batch = NotificationBatchEntity.builder()
          .template(template)
          .statusCode(NotificationBatchStatusEnum.PENDING)
          .placeholders(Map.of("name", "Hans"))
          .build();
      entityManager.persist(batch);

      var recipient = NotificationRecipientEntity.builder().batch(batch).userId(UUID.randomUUID()).email("test@example.com").build();
      entityManager.persist(recipient);

      recipientId = recipient.getId();
      batchId = batch.getId();
    });
  }

  @Test
  void givenValidTemplateAndRecipient_whenDispatch_thenSubmittedDispatchEntityPersistedInDb() {
    QuarkusTransaction.requiringNew().run(() -> {
      var batch = entityManager.find(NotificationBatchEntity.class, batchId);
      var resolved = placeholderService.resolve(batch);
      service.dispatch(entityManager.find(NotificationRecipientEntity.class, recipientId), resolved);
    });

    verify(emailApi).submitEmail(eq("test@example.com"), eq("Hello Hans"), argThat(body -> body.contains("<p>Dear Hans</p>")));

    List<NotificationDispatchEntity> dispatches =
        QuarkusTransaction.requiringNew().call(() -> dispatchRepository.list("recipient.id = ?1", recipientId));

    assertThat(dispatches).hasSize(1).first().satisfies(d -> {
      assertThat(d.getStatusCode()).isEqualTo(NotificationDispatchStatusEnum.SUBMITTED);
      assertThat(d.getError()).isNull();
    });
  }

  @Test
  void givenEmailApiThrows_whenDispatch_thenFailedDispatchEntityPersistedInDb() {
    doThrow(new ExternalWebServiceException("SES unavailable", new RuntimeException("timeout"))).when(emailApi)
        .submitEmail(any(), any(), any());

    boolean result = QuarkusTransaction.requiringNew().call(() -> {
      var batch = entityManager.find(NotificationBatchEntity.class, batchId);
      var resolved = placeholderService.resolve(batch);
      return service.dispatch(entityManager.find(NotificationRecipientEntity.class, recipientId), resolved);
    });

    assertThat(result).isFalse();

    List<NotificationDispatchEntity> dispatches =
        QuarkusTransaction.requiringNew().call(() -> dispatchRepository.list("recipient.id = ?1", recipientId));

    assertThat(dispatches).hasSize(1).first().satisfies(d -> {
      assertThat(d.getStatusCode()).isEqualTo(NotificationDispatchStatusEnum.FAILED);
      assertThat(d.getError()).isEqualTo("SES unavailable");
    });
  }
}
