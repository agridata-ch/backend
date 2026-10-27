package ch.agridata.user.persistence;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.persistence.BaseSearchRepository;
import ch.agridata.common.persistence.SearchField;
import ch.agridata.common.persistence.SearchSpec;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for the AgbRevisionEntity. It is used to find the current revision of the agb text
 * and to page through all revisions.
 *
 * @CommentLastReviewed 2026-10-01
 */
@ApplicationScoped
public class AgbRevisionRepository extends BaseSearchRepository<AgbRevisionEntity, UUID> {

  private static final Map<String, SearchField> SORTABLE_FIELDS = Map.of(
      "version", SearchField.simple("version"),
      "validFrom", SearchField.simple("validFrom"),
      "validTo", SearchField.simple("validTo"),
      "enforceConsentFrom", SearchField.simple("enforceConsentFrom")
  );

  public PageResponseDto<AgbRevisionEntity> findPaged(ResourceQueryDto query) {
    return findPage(query, SearchSpec.builder().sortableFields(SORTABLE_FIELDS).build());
  }

  public boolean existsByVersion(String version) {
    return count("version", version) > 0;
  }

  public Optional<AgbRevisionEntity> findOpenRevision() {
    return find("validTo is null").singleResultOptional();
  }

  public Optional<AgbRevisionEntity> findValidRevisionAt(LocalDateTime dateTimeNow) {
    return find(
        """
            archived = false
            and validFrom <= :dateTimeNow
            and (validTo is null or validTo > :dateTimeNow)
            """,
        Map.of("dateTimeNow", dateTimeNow)
    ).singleResultOptional();
  }
}
