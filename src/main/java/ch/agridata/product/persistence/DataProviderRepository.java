package ch.agridata.product.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides persistence operations for data providers.
 *
 * @CommentLastReviewed 2026-02-06
 */
@ApplicationScoped
public class DataProviderRepository implements PanacheRepositoryBase<DataProviderEntity, UUID> {
  public Optional<DataProviderEntity> findByUidOptional(String uid) {
    return find("uid", uid).firstResultOptional();
  }
}
