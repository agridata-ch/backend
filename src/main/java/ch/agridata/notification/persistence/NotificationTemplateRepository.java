package ch.agridata.notification.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides persistence operations for notification templates.
 *
 * @CommentLastReviewed 2026-04-22
 */
@ApplicationScoped
public class NotificationTemplateRepository implements PanacheRepositoryBase<NotificationTemplateEntity, UUID> {

  /**
   * Finds the latest version of a template for the given event type code.
   */
  public Optional<NotificationTemplateEntity> findLatestByEventTypeCode(String eventTypeCode) {
    return find("eventTypeCode = ?1 ORDER BY templateVersion DESC", eventTypeCode)
        .firstResultOptional();
  }
}
