package ch.agridata.product.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository class for performing operations on RestClientEntity. It uses PanacheRepositoryBase to provide
 * CRUD functionalities and additional query methods specific to RestClientEntity.
 *
 * @CommentLastReviewed 2026-06-11
 */

@ApplicationScoped
public class RestClientRepository implements PanacheRepositoryBase<RestClientEntity, UUID> {
  public Optional<RestClientEntity> findByIdAndProviderUidOptional(UUID restClientId, String providerUid) {
    return find("""
        select rc from DataProviderEntity dp
        join dp.restClients rc
        where rc.id = ?1
        and dp.uid = ?2
        """, restClientId, providerUid)
        .firstResultOptional();
  }
}
