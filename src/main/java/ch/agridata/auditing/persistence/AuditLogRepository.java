package ch.agridata.auditing.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

/**
 * Provides database access for audit log entities.
 *
 * @CommentLastReviewed 2025-08-25
 */

@ApplicationScoped
public class AuditLogRepository implements PanacheRepositoryBase<AuditLogEntity, UUID> {

}
