package ch.agridata.user.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for the AgbRevisionEntity. It is used to find the current revision of the agb text.
 *
 * @CommentLastReviewed 2026-07-16
 */

@ApplicationScoped
public class AgbRevisionRepository implements PanacheRepositoryBase<AgbRevisionEntity, UUID> {

  public Optional<AgbRevisionEntity> findValidRevisionAt(LocalDateTime dateTimeNow) {
    return find("""
            archived = false
            and validFrom <= :dateTimeNow
            and (validTo is null or validTo >= :dateTimeNow)
            """,
        Map.of("dateTimeNow", dateTimeNow)).singleResultOptional();
  }
}
