package ch.agridata.product.persistence;

import ch.agridata.common.persistence.TranslationPersistenceDto;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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

  public Optional<DataProviderEntity> findByIdAndProviderUidOptional(UUID id, String uid) {
    return find("id = ?1 and uid = ?2", id, uid).firstResultOptional();
  }
  
  public Map<String, TranslationPersistenceDto> findNamesByUids(Collection<String> uids) {
    if (uids.isEmpty()) {
      return Map.of();
    }

    Map<String, TranslationPersistenceDto> namesByUid = new HashMap<>();
    find("uid in ?1", uids).list().forEach(provider -> namesByUid.putIfAbsent(provider.getUid(), provider.getName()));
    return namesByUid;
  }
}
