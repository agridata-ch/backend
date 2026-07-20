package ch.agridata.product.persistence;

import io.quarkus.hibernate.panache.PanacheRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Query;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides persistence operations for data providers. The {@link Find}/{@link Query} methods are validated against the entity model at
 * compile time by the Hibernate annotation processor, while the managed (stateful) session preserves lazy loading of associations such as
 * {@link DataProviderEntity#getRestClients()}.
 *
 * @CommentLastReviewed 2026-07-20
 */
public interface DataProviderRepository extends PanacheRepository.Managed<DataProviderEntity, UUID> {
  @Find
  Optional<DataProviderEntity> findByUidOptional(String uid);

  @Query("where id = :id and uid = :uid")
  Optional<DataProviderEntity> findByIdAndProviderUidOptional(UUID id, String uid);
}
