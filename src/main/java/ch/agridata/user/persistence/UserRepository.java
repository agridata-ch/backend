package ch.agridata.user.persistence;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.persistence.BaseSearchRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
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

  public PageResponseDto<UserEntity> findByRoleAtLastLogin(ResourceQueryDto resourceQueryDto, String role) {
    return findPage(
        resourceQueryDto,
        "function('jsonb_exists', rolesAtLastLogin, :role) = true",
        Map.of("role", role),
        FILTER_FIELDS,
        COMBINED_FIELDS
    );
  }

  public Optional<UserEntity> findByAgateLoginId(String agateLoginId) {
    return find("agateLoginId", agateLoginId).firstResultOptional();
  }
}
