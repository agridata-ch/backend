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

  public static final String EMAIL = "email";
  public static final String GIVEN_NAME = "givenName";
  public static final String FAMILY_NAME = "familyName";
  public static final String PHONE_NUMBER = "phoneNumber";
  public static final String LAST_LOGIN_DATE = "lastLoginDate";
  public static final SearchField FIELD_EMAIL = SearchField.simple(EMAIL);
  public static final SearchField FIELD_GIVEN_NAME = SearchField.simple(GIVEN_NAME);
  public static final SearchField FIELD_FAMILY_NAME = SearchField.simple(FAMILY_NAME);
  public static final SearchField FIELD_PHONE_NUMBER = SearchField.simple(PHONE_NUMBER);
  public static final SearchField FIELD_LAST_LOGIN_DATE = SearchField.simple(LAST_LOGIN_DATE);

  private static final List<SearchField> SEARCHABLE_FIELDS = List.of(FIELD_EMAIL, FIELD_GIVEN_NAME, FIELD_FAMILY_NAME, FIELD_PHONE_NUMBER);
  private static final List<List<SearchField>> COMBINED_FIELDS = List.of(List.of(FIELD_GIVEN_NAME, FIELD_FAMILY_NAME));
  private static final Map<String, SearchField> SORTABLE_FIELDS = Map.of(
      EMAIL, FIELD_EMAIL,
      GIVEN_NAME, FIELD_GIVEN_NAME,
      FAMILY_NAME, FIELD_FAMILY_NAME,
      PHONE_NUMBER, FIELD_PHONE_NUMBER,
      LAST_LOGIN_DATE, FIELD_LAST_LOGIN_DATE
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
