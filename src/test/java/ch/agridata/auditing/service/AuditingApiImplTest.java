package ch.agridata.auditing.service;

import static ch.agridata.auditing.api.ActionEnum.CONSENT_REQUEST_GRANTED;
import static ch.agridata.auditing.api.EntityTypeEnum.CONSENT_REQUEST;
import static ch.agridata.auditing.persistence.AuditLogEntity.ActorTypeEnum.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.auditing.persistence.AuditLogEntity;
import ch.agridata.auditing.persistence.AuditLogRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.ws.rs.NotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.jboss.logging.MDC;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditingApiImplTest {

  @Mock
  AgridataSecurityIdentity identity;
  @Mock
  AuditLogRepository repository;
  @Mock
  Clock clock;
  @InjectMocks
  AuditingApiImpl auditingApi;

  @Test
  void givenLoggedInUser_whenLogUserAction_thenPersistAuditLog() {
    MDC.put("requestId", "test-request-id");
    UUID entityId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(identity.getUserId()).thenReturn(userId);
    when(clock.instant()).thenReturn(Instant.parse("2025-01-01T10:00:00Z"));
    when(clock.getZone()).thenReturn(ZoneOffset.UTC);

    auditingApi.logUserAction(
        CONSENT_REQUEST_GRANTED,
        CONSENT_REQUEST,
        entityId
    );

    ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
    verify(repository).persist(captor.capture());

    AuditLogEntity auditLogEntity = captor.getValue();
    assertThat(auditLogEntity)
        .usingRecursiveComparison()
        .isEqualTo(AuditLogEntity.builder()
            .actorId(userId.toString())
            .entityTypeCode(CONSENT_REQUEST.name())
            .actionCode(CONSENT_REQUEST_GRANTED.name())
            .entityId(entityId)
            .actorTypeCode(USER)
            .timestamp(LocalDateTime.now(clock))
            .requestId("test-request-id")
            .build());
  }

  @Test
  void givenLoggedOutUser_whenLogUserAction_thenThrowNotFoundException() {
    when(identity.getUserId()).thenReturn(null);
    UUID entityId = UUID.randomUUID();

    assertThatThrownBy(() -> auditingApi.logUserAction(
        CONSENT_REQUEST_GRANTED,
        CONSENT_REQUEST,
        entityId)).isInstanceOf(NotFoundException.class);

    verify(repository, never()).persist(any(AuditLogEntity.class));
  }
}
