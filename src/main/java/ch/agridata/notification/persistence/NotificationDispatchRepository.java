package ch.agridata.notification.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

/**
 * Provides persistence operations for notification dispatch records.
 *
 * @CommentLastReviewed 2026-04-22
 */
@ApplicationScoped
public class NotificationDispatchRepository implements PanacheRepositoryBase<NotificationDispatchEntity, UUID> {

}
