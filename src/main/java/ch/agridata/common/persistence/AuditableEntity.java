package ch.agridata.common.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Provides a base entity with auditing fields. It enables consistent timestamp and user tracking.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

  @Column(name = "archived", nullable = false)
  private boolean archived;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(name = "modified_by")
  private UUID modifiedBy;

  @Column(name = "modified_at", nullable = false)
  @UpdateTimestamp
  private LocalDateTime modifiedAt;
}
