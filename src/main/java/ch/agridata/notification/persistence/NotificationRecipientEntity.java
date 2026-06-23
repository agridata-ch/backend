package ch.agridata.notification.persistence;

import ch.agridata.common.dto.SupportedLanguage;
import ch.agridata.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Represents a single recipient (user or e-mail address) within a notification batch. A recipient
 * is identified either by their system {@code userId}, their {@code email}, or both.
 *
 * @CommentLastReviewed 2026-04-22
 */
@Entity
@Table(
    name = "notification_recipient",
    indexes = {
        @Index(name = "idx_notification_recipient_batch_id", columnList = "batch_id"),
        @Index(name = "idx_notification_recipient_user_id", columnList = "user_id"),
    }
)
@SQLDelete(sql = "UPDATE notification_recipient SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecipientEntity extends AuditableEntity {

  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "batch_id", nullable = false)
  private NotificationBatchEntity batch;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "email")
  private String email;

  @Column(name = "language", length = 2)
  @Enumerated(EnumType.STRING)
  private SupportedLanguage language;

}
