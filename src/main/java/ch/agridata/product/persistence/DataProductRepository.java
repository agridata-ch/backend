package ch.agridata.product.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

/**
 * Provides persistence operations for data products.
 *
 * @CommentLastReviewed 2025-08-25
 */

@ApplicationScoped
public class DataProductRepository implements PanacheRepositoryBase<DataProductEntity, UUID> {
}
