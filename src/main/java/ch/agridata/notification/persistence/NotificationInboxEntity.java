package ch.agridata.notification.persistence;

import ch.agridata.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Represents a user-facing inbox entry for in-(web)app notifications.
 *
 * @CommentLastReviewed 2026-05-08
 */
@Entity
@Table(name = "notification_inbox",
    indexes = {
        @Index(name = "idx_notification_inbox_recipient_id", columnList = "recipient_id"),
        @Index(name = "idx_notification_inbox_user_id", columnList = "user_id"),
        @Index(name = "idx_notification_inbox_is_read", columnList = "is_read"),
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_notification_inbox_recipient_id", columnNames = "recipient_id"),
    })
@SQLDelete(sql = "UPDATE notification_inbox SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationInboxEntity extends AuditableEntity {

  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "recipient_id", nullable = false)
  private NotificationRecipientEntity recipient;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "is_read", nullable = false)
  private boolean isRead;
}
