package ch.agridata.common.persistence;

import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * A listener that sets common auditing fields (created by, modified by) before a database operation takes place.
 *
 * @CommentLastReviewed 2025-08-25
 */

@ApplicationScoped
@RequiredArgsConstructor
public class AuditingEntityListener {

  private final AgridataSecurityIdentity agridataSecurityIdentity;

  @PrePersist
  public void setCreatedFields(AuditableEntity entity) {
    UUID userId = agridataSecurityIdentity.getUserId();
    entity.setCreatedBy(userId);
    entity.setModifiedBy(userId);
  }

  @PreUpdate
  public void setModifiedFields(AuditableEntity entity) {
    entity.setModifiedBy(agridataSecurityIdentity.getUserId());
  }
}
