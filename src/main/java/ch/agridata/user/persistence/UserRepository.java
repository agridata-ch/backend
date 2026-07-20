package ch.agridata.user.persistence;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.persistence.BaseSearchRepository;
import com.google.common.collect.Collections2;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.Order;
import org.hibernate.query.Page;
import org.hibernate.query.restriction.Restriction;
import org.hibernate.query.specification.SelectionSpecification;

/**
 * Provides repository access for managing user entities.
 *
 * @CommentLastReviewed 2026-07-20
 */

@ApplicationScoped
public class UserRepository extends BaseSearchRepository<UserEntity, UUID> {

  // JSONB predicate stays a runtime HQL fragment (not expressible via the type-safe metamodel); only the search + sort become type-safe.
  // Hibernate 7's native json_exists() would validate the grammar at boot, but it is a tech-preview function disabled by default
  // (needs hibernate.query.hql.json_functions_enabled=true), so we keep the function('jsonb_exists', ...) passthrough that works today.
  private static final String ROLE_AT_LAST_LOGIN = "from UserEntity where function('jsonb_exists', rolesAtLastLogin, :role) = true";

  /**
   * Prototype of the Hibernate 7 {@link SelectionSpecification} approach: the JSONB role predicate remains HQL, while the free-text search
   * ({@link Restriction}) and sorting ({@link Order}) reference the static metamodel and are therefore compile-time checked and
   * refactoring-safe. Contrast with {@link BaseSearchRepository#findPage} which builds the same query from strings.
   */
  public PageResponseDto<UserEntity> findByRoleAtLastLogin(ResourceQueryDto query, String role) {
    var spec = SelectionSpecification.create(UserEntity.class, ROLE_AT_LAST_LOGIN)
        .restrict(searchRestriction(query.searchTerm()));
    for (var order : toOrders(query.sortParams())) {
      spec = spec.sort(order);
    }

    var jpaQuery = spec.createQuery(getEntityManager()).setParameter("role", role);

    var totalItems = jpaQuery.getResultCount();
    var items = jpaQuery.setPage(Page.page(query.size(), query.page())).getResultList();
    var totalPages = (int) Math.ceil((double) totalItems / query.size());

    return new PageResponseDto<>(items, totalItems, totalPages, query.page(), query.size());
  }

  /**
   * Builds the case-insensitive "contains" search: each field matches the whole term, and every combined-field group matches any
   * permutation of the search tokens (e.g. "sm Jo" matches familyName "Smith" + givenName "John"). Mirrors {@code JpaUtil}, but with
   * metamodel attributes instead of field-name strings.
   */
  private Restriction<UserEntity> searchRestriction(String searchTerm) {
    if (StringUtils.isBlank(searchTerm)) {
      return Restriction.unrestricted();
    }
    var cleaned = clean(searchTerm);
    var tokens = List.of(cleaned.split("\\s+"));

    var perField = Stream.of(UserEntity_.email, UserEntity_.givenName, UserEntity_.familyName, UserEntity_.phoneNumber)
        .map(field -> Restriction.contains(field, cleaned, false));

    Stream<Restriction<UserEntity>> combined = tokens.size() < 2
        ? Stream.empty()
        : Collections2.permutations(List.of(UserEntity_.givenName, UserEntity_.familyName)).stream()
            .map(perm -> Restriction.all(IntStream.range(0, perm.size())
                .mapToObj(i -> Restriction.contains(perm.get(i), tokens.get(i), false))
                .toList()));

    return Restriction.any(Stream.concat(perField, combined).toList());
  }

  /**
   * Parses REST sort params ("field" asc, "-field" desc) into type-safe {@link Order}s. The allow-list of sortable attributes doubles as
   * validation — an unknown field is rejected instead of being interpolated into a query string.
   */
  private List<Order<UserEntity>> toOrders(List<String> sortParams) {
    if (sortParams == null) {
      return List.of();
    }
    return sortParams.stream()
        .flatMap(param -> Arrays.stream(param.split(",")))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .map(this::toOrder)
        .toList();
  }

  private Order<UserEntity> toOrder(String token) {
    var desc = token.startsWith("-");
    var field = desc ? token.substring(1) : token;
    final SingularAttribute<UserEntity, ?> attribute = switch (field) {
      case "lastLoginDate" -> UserEntity_.lastLoginDate;
      case "email" -> UserEntity_.email;
      case "familyName" -> UserEntity_.familyName;
      case "givenName" -> UserEntity_.givenName;
      default -> throw new IllegalArgumentException("Unsupported sort field: " + field);
    };
    return desc ? Order.desc(attribute) : Order.asc(attribute);
  }

  private static String clean(String input) {
    return input.toLowerCase(Locale.ROOT).trim().replace("%", "").replace("_", "").replace("*", "");
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
