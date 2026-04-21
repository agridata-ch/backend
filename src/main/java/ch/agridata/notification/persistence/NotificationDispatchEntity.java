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
 * Tracks actual delivery attempts and outcomes for EMAIL or MOBILE push notifications per recipient.
 *
 * @CommentLastReviewed 2026-04-22
 */
@Entity
@Table(name = "notification_dispatch",
    indexes = {
        @Index(name = "idx_notification_dispatch_recipient_id", columnList = "recipient_id"),
        @Index(name = "idx_notification_dispatch_status_code", columnList = "status_code"),
    })
@SQLDelete(sql = "UPDATE notification_dispatch SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDispatchEntity extends AuditableEntity {

  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recipient_id", nullable = false)
  private NotificationRecipientEntity recipient;

  @Column(name = "channel_code", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private NotificationChannelCodeEnum channelCode;

  @Column(name = "status_code", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private NotificationDispatchStatusEnum statusCode;

  @Column(name = "error", length = 1000)
  private String error;
}
