package ch.agridata.product.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

/**
 * Provides persistence operations for data source systems.
 *
 * @CommentLastReviewed 2026-02-06
 */
@ApplicationScoped
public class DataSourceSystemRepository implements PanacheRepositoryBase<DataSourceSystemEntity, UUID> {
}
