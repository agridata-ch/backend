package ch.agridata.notification.persistence;

import ch.agridata.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

/**
 * Holds queued notification batches to be delivered. Each row is a concrete job generated from a
 * template for a group of recipients.
 *
 * @CommentLastReviewed 2026-04-22
 */
@Entity
@Table(name = "notification_batch",
    indexes = {
        @Index(name = "idx_notification_batch_template_id", columnList = "template_id"),
        @Index(name = "idx_notification_batch_status_code", columnList = "status_code"),
    }
)
@SQLDelete(sql = "UPDATE notification_batch SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationBatchEntity extends AuditableEntity {

  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "template_id", nullable = false)
  private NotificationTemplateEntity template;

  @Column(name = "placeholders")
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, String> placeholders;

  @Column(name = "status_code", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private NotificationBatchStatusEnum statusCode;

  @Column(name = "target_type_code", length = 50)
  @Enumerated(EnumType.STRING)
  private TargetTypeCodeEnum targetTypeCode;

  @Column(name = "target_id")
  private UUID targetId;
}
