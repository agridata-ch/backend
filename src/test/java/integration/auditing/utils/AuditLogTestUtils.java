package integration.auditing.utils;

import ch.agridata.auditing.persistence.AuditLogEntity;
import ch.agridata.auditing.persistence.AuditLogRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class AuditLogTestUtils {

  private final AuditLogRepository auditLogRepository;

  public AuditLogEntity getLatestAuditLogEntry() {
    return getLatestAuditLogEntry(0);
  }

  public AuditLogEntity getLatestAuditLogEntry(int offset) {
    return auditLogRepository
        .findAll(Sort.descending("timestamp"))
        .page(offset, 1)
        .firstResult();
  }

}
