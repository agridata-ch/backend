package ch.agridata.auditing.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Represents an audit log entry in the persistence layer. It captures metadata such as timestamp, actor, action, entity details, and
 * request context, with built-in support for soft deletion.
 *
 * @CommentLastReviewed 2025-08-25
 */
@Entity
@Table(name = "audit_log",
    indexes = {
        @Index(name = "idx_audit_log_actor_id", columnList = "actor_id"),
        @Index(name = "idx_audit_log_request_id", columnList = "request_id")
    })
@SQLDelete(sql = "UPDATE audit_log SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  @GeneratedValue
  private UUID id;

  @Column(name = "archived", nullable = false)
  private boolean archived;

  @Column(name = "timestamp", nullable = false, updatable = false)
  @CreationTimestamp
  private LocalDateTime timestamp;

  @Column(name = "actor_type_code", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private ActorTypeEnum actorTypeCode;

  @Column(name = "actor_id")
  private String actorId;

  @Column(name = "action_code", nullable = false, length = 50)
  private String actionCode;

  @Column(name = "entity_type_code", nullable = false, length = 50)
  private String entityTypeCode;

  @Column(name = "entity_id")
  private UUID entityId;

  @Column(name = "request_id")
  private String requestId;

  /**
   * Enumerates supported types of actors.
   *
   * @CommentLastReviewed 2025-08-25
   */
  public enum ActorTypeEnum {
    USER,
    SYSTEM
  }
}
