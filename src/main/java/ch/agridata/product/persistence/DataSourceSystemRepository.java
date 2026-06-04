package ch.agridata.product.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides persistence operations for data source systems.
 *
 * @CommentLastReviewed 2026-02-06
 */
@ApplicationScoped
public class DataSourceSystemRepository implements PanacheRepositoryBase<DataSourceSystemEntity, UUID> {

  public Optional<DataSourceSystemEntity> findByIdAndProviderUidOptional(UUID dataSourceSystemId, String dataProviderUid) {
    return find("""
        select dss
        from DataSourceSystemEntity dss
        join dss.dataProvider dp
        WHERE dss.id = ?1 AND dp.uid = ?2
        """, dataSourceSystemId, dataProviderUid).firstResultOptional();
  }
}
