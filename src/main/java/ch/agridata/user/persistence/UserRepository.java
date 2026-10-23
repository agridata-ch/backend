package ch.agridata.user.persistence;

import static ch.agridata.user.persistence.AgbAcceptanceStatusEnum.ACCEPTED;
import static ch.agridata.user.persistence.AgbAcceptanceStatusEnum.PENDING;
import static ch.agridata.user.persistence.AgbAcceptanceStatusEnum.TRANSITION_PERIOD;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.persistence.BaseSearchRepository;
import ch.agridata.common.persistence.FilterField;
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
  public static final String UID = "uid";
  public static final String LAST_ACCEPTED_AGB_DATE = "lastAcceptedAgbDate";
  public static final String AGB_ACCEPTANCE_STATUS = "status";
  public static final SearchField FIELD_UID = SearchField.simple(UID);
  public static final SearchField FIELD_LAST_ACCEPTED_AGB_DATE = SearchField.simple(LAST_ACCEPTED_AGB_DATE);
  private static final String AGB_ACCEPTANCE_STATUS_EXPRESSION = """
      case
        when enforceAgbAcceptanceFrom is null then '%s'
        when enforceAgbAcceptanceFrom > local datetime then '%s'
        else '%s'
      end""".formatted(ACCEPTED, TRANSITION_PERIOD, PENDING);
  private static final FilterField FIELD_AGB_ACCEPTANCE_STATUS =
      new FilterField(AGB_ACCEPTANCE_STATUS_EXPRESSION, value -> AgbAcceptanceStatusEnum.valueOf(value).name());

  private static final List<SearchField> SEARCHABLE_FIELDS = List.of(FIELD_EMAIL, FIELD_GIVEN_NAME, FIELD_FAMILY_NAME, FIELD_PHONE_NUMBER);
  private static final Map<String, SearchField> SORTABLE_FIELDS = Map.of(
      EMAIL, FIELD_EMAIL,
      GIVEN_NAME, FIELD_GIVEN_NAME,
      FAMILY_NAME, FIELD_FAMILY_NAME,
      PHONE_NUMBER, FIELD_PHONE_NUMBER,
      LAST_LOGIN_DATE, FIELD_LAST_LOGIN_DATE
  );
  private static final Map<String, SearchField> AGB_ACCEPTANCE_SORTABLE_FIELDS = Map.of(
      EMAIL, FIELD_EMAIL,
      GIVEN_NAME, FIELD_GIVEN_NAME,
      FAMILY_NAME, FIELD_FAMILY_NAME,
      UID, FIELD_UID,
      LAST_ACCEPTED_AGB_DATE, FIELD_LAST_ACCEPTED_AGB_DATE
  );

  public PageResponseDto<UserEntity> findByRoleAtLastLogin(ResourceQueryDto resourceQueryDto, String role) {
    return findPage(
        resourceQueryDto,
        SearchSpec.builder()
            .baseWhere("function('jsonb_exists', rolesAtLastLogin, :role) = true")
            .baseParams(Map.of("role", role))
            .searchableFields(SEARCHABLE_FIELDS)
            .sortableFields(SORTABLE_FIELDS)
            .build()
    );
  }

  /**
   * Find a page of users who accepted the AGB at least once, together with their AGB acceptance state. Only data consumers and data
   * providers can accept the AGB, so no role filter is needed. The partial index {@code idx_users_agb_accepted} narrows the query to
   * these users. The search term is matched against the UID only. The {@code status} filter must derive the state the same way as
   * {@code UserService#toAgbAcceptanceStatus}.
   */
  public PageResponseDto<UserEntity> findAgbAcceptancePage(ResourceQueryDto resourceQueryDto) {
    return findPage(
        resourceQueryDto,
        SearchSpec.builder()
            .baseWhere("lastAcceptedAgbDate is not null")
            .searchableFields(List.of(FIELD_UID))
            .filterableFields(Map.of(AGB_ACCEPTANCE_STATUS, FIELD_AGB_ACCEPTANCE_STATUS))
            .sortableFields(AGB_ACCEPTANCE_SORTABLE_FIELDS)
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
