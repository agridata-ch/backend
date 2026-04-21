package ch.agridata.notification.persistence;

import ch.agridata.common.persistence.AuditableEntity;
import ch.agridata.common.persistence.TranslationPersistenceDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.List;
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
 * Defines reusable message templates keyed to a notification event type.
 *
 * @CommentLastReviewed 2026-04-22
 */
@Entity
@Table(name = "notification_template",
    indexes = {
        @Index(name = "idx_notification_template_event_type_code", columnList = "event_type_code"),
    })
@SQLDelete(sql = "UPDATE notification_template SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateEntity extends AuditableEntity {

  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "event_type_code", nullable = false, length = 50)
  private String eventTypeCode;

  @Column(name = "template_version", nullable = false)
  private int templateVersion;

  @Column(name = "email_subject")
  @JdbcTypeCode(SqlTypes.JSON)
  private TranslationPersistenceDto emailSubject;

  @Column(name = "email_text")
  @JdbcTypeCode(SqlTypes.JSON)
  private TranslationPersistenceDto emailText;

  @Column(name = "webapp_text")
  @JdbcTypeCode(SqlTypes.JSON)
  private TranslationPersistenceDto webappText;

  @Column(name = "mobile_text")
  @JdbcTypeCode(SqlTypes.JSON)
  private TranslationPersistenceDto mobileText;

  @Column(name = "required_generic_placeholders")
  @JdbcTypeCode(SqlTypes.JSON)
  private List<String> requiredGenericPlaceholders;
}
