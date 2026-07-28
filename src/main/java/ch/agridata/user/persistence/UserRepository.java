package ch.agridata.user.persistence;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.persistence.BaseSearchRepository;
import ch.agridata.common.persistence.SearchField;
import ch.agridata.common.persistence.SearchSpec;
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

  private static final List<SearchField> SEARCHABLE_FIELDS = List.of(
      SearchField.simple("email"),
      SearchField.simple("givenName"),
      SearchField.simple("familyName"),
      SearchField.simple("phoneNumber")
  );
  private static final List<List<SearchField>> COMBINED_FIELDS = List.of(
      List.of(SearchField.simple("givenName"), SearchField.simple("familyName"))
  );
  private static final Map<String, SearchField> SORTABLE_FIELDS = Map.of(
      "email", SearchField.simple("email"),
      "givenName", SearchField.simple("givenName"),
      "familyName", SearchField.simple("familyName"),
      "phoneNumber", SearchField.simple("phoneNumber"),
      "lastLoginDate", SearchField.simple("lastLoginDate")
  );

  public PageResponseDto<UserEntity> findByRoleAtLastLogin(ResourceQueryDto resourceQueryDto, String role) {
    return findPage(
        resourceQueryDto,
        SearchSpec.builder()
            .baseWhere("function('jsonb_exists', rolesAtLastLogin, :role) = true")
            .baseParams(Map.of("role", role))
            .searchableFields(SEARCHABLE_FIELDS)
            .combinedFields(COMBINED_FIELDS)
            .sortableFields(SORTABLE_FIELDS)
            .build()
    );
  }

  public Optional<UserEntity> findByAgateLoginId(String agateLoginId) {
    return find("agateLoginId", agateLoginId).firstResultOptional();
  }

  public List<UserEntity> findAllByRoleAtLastLogin(String role) {
    return find("function('jsonb_exists', rolesAtLastLogin, :role) = true", Map.of("role", role)).list();
  }

  public List<UserEntity> findAllByRoleAtLastLoginAndUid(String role, String uid) {
    return find(
        "function('jsonb_exists', rolesAtLastLogin, :role) = true and uid = :uid",
        Map.of("role", role, "uid", uid)
    ).list();
  }
}
