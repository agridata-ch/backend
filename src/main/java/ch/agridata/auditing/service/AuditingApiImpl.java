package ch.agridata.auditing.service;

import static ch.agridata.auditing.persistence.AuditLogEntity.ActorTypeEnum.USER;
import static ch.agridata.common.filters.PreSecurityMdcFilter.REQUEST_ID_MDC_FIELD;

import ch.agridata.auditing.api.ActionEnum;
import ch.agridata.auditing.api.AuditingApi;
import ch.agridata.auditing.api.EntityTypeEnum;
import ch.agridata.auditing.persistence.AuditLogEntity;
import ch.agridata.auditing.persistence.AuditLogRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.MDC;

/**
 * Provides the concrete implementation of the auditing API. It records user actions by combining identity information, request metadata,
 * and action details into persistent audit logs.
 *
 * @CommentLastReviewed 2025-08-25
 */

@ApplicationScoped
@RequiredArgsConstructor
public class AuditingApiImpl implements AuditingApi {

  private final AgridataSecurityIdentity identity;
  private final AuditLogRepository repository;
  private final Clock clock;

  @Override
  @Transactional
  public void logUserAction(ActionEnum actionCode,
                            EntityTypeEnum entityTypeCode,
                            UUID entityId) {
    var userId = identity.getUserId();
    if (userId == null) {
      throw new NotFoundException();
    }
    var auditLog = AuditLogEntity.builder()
        .timestamp(LocalDateTime.now(clock))
        .actorTypeCode(USER)
        .actorId(userId.toString())
        .actionCode(actionCode.name())
        .entityTypeCode(entityTypeCode.name())
        .entityId(entityId)
        .requestId(MDC.get(REQUEST_ID_MDC_FIELD).toString())
        .build();
    repository.persist(auditLog);
  }
}
