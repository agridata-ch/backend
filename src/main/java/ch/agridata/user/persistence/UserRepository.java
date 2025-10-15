package ch.agridata.user.persistence;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.persistence.BaseSearchRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides repository access for managing user entities.
 *
 * @CommentLastReviewed 2025-09-08
 */

@ApplicationScoped
public class UserRepository extends BaseSearchRepository<UserEntity, UUID> {

  private static final List<String> FILTER_FIELDS = List.of("email", "givenName", "familyName", "phoneNumber");
  private static final List<List<String>> COMBINED_FIELDS = List.of(List.of("givenName", "familyName"));

  public PageResponseDto<UserEntity> findByKtIdpNotNull(ResourceQueryDto resourceQueryDto) {
    return findPage(
        resourceQueryDto,
        "ktIdP is not null",
        null,
        FILTER_FIELDS,
        COMBINED_FIELDS
    );
  }

  public Optional<UserEntity> findByKtIdP(String impersonatedKtIdP) {
    return find("ktIdP", impersonatedKtIdP).firstResultOptional();
  }
}
