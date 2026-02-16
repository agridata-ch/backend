package ch.agridata.product.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.UUID;

/**
 * Provides persistence operations for data products.
 *
 * @CommentLastReviewed 2025-08-25
 */

@ApplicationScoped
public class DataProductRepository implements PanacheRepositoryBase<DataProductEntity, UUID> {
  public List<DataProductEntity> listByProviderId(UUID providerId) {
    return find("""
        select dp
        from DataProductEntity dp
        join dp.dataSourceSystem ds
        join ds.dataProvider p
        where p.id = ?1
        order by ds.code, dp.id
        """, providerId).list();
  }

  public UUID findDataSourceSystemIdByProductId(UUID productId) {
    return find("""
        select dp.dataSourceSystem.id
        from DataProductEntity dp
        where dp.id = ?1
        """, productId)
        .project(UUID.class)
        .firstResultOptional()
        .orElseThrow(() -> new NotFoundException(productId.toString()));
  }
}
