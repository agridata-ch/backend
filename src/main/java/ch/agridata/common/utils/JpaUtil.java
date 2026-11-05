package ch.agridata.common.utils;

import io.quarkus.panache.common.Sort;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

/**
 * Utility class for JPA-related operations, such as building dynamic queries and parsing sort parameters.
 *
 * @CommentLastReviewed 2025-09-11
 */
@UtilityClass
public class JpaUtil {

  private static final String PARAM_PREFIX = "param";
  public static final String DUMMY_WHERE_CLAUSE = "1=1";

  /**
   * Parse sort parameters in REST Data Panache style:
   * - "field"  -> ascending
   * - "-field" -> descending
   * Example:
   * parseSort(List.of("name", "-age"))
   * => ORDER BY name ASC, age DESC
   */
  public static Sort parseSort(List<String> sortParams) {
    if (sortParams == null || sortParams.isEmpty()) {
      return Sort.empty();
    }

    Sort sort = Sort.empty();

    for (String s : sortParams) {
      if (s == null || s.isBlank()) {
        continue;
      }
      var trimmed = s.trim();
      if (trimmed.startsWith("-")) {
        sort = sort.and(trimmed.substring(1).trim(), Sort.Direction.Descending);
      } else {
        sort = sort.and(trimmed);
      }
    }

    return sort;
  }

  /**
   * Create WHERE clause for a SQL query that matches if every whitespace-separated token of the searchTerm
   * is contained in at least one of the specified fields, in any order,
   * e.g. "anna maria muster" matches firstName "Anna Maria" and name "Muster".
   * The search is case-insensitive and uses the SQL LIKE operator with wildcards.
   *
   * @param searchTerm The searchTerm string to search for.
   * @param fields     The list of fields to search in.
   * @return A Where clause.
   */
  public static WhereClause createContainsWhereClause(String searchTerm, List<String> fields) {
    if (searchTerm == null || fields.isEmpty()) {
      return createDummyWhereClause();
    }
    // A repeated token adds no condition, as every token must match anyway
    var tokens = Arrays.stream(cleanSearchString(searchTerm).split("\\s+")).distinct().toList();
    var clauses = new ArrayList<String>();
    var parameters = new HashMap<String, Object>();

    for (var i = 0; i < tokens.size(); i++) {
      var parameterName = PARAM_PREFIX + i;
      parameters.put(parameterName, createContainsValue(tokens.get(i)));
      clauses.add(fields.stream()
          .map(field -> createWhereClause(field, parameterName))
          .collect(Collectors.joining(" OR ", "(", ")")));
    }

    return new WhereClause(String.join(" AND ", clauses), parameters);
  }

  private static String createWhereClause(String field, String parameterName) {
    return "LOWER(" + field + ") LIKE :" + parameterName;
  }

  private static String cleanSearchString(String input) {
    if (input == null) {
      return "";
    }
    return input
        .toLowerCase(Locale.ROOT)
        .trim()
        .replace("%", "")
        .replace("_", "")
        .replace("*", "");
  }

  private static String createContainsValue(String value) {
    return "%" + value + "%";
  }

  private static WhereClause createDummyWhereClause() {
    return new WhereClause(DUMMY_WHERE_CLAUSE, Map.of());
  }

  /**
   * A record representing a WHERE clause and its associated parameters.
   *
   * @CommentLastReviewed 2025-09-11
   */
  public record WhereClause(String clause, Map<String, Object> parameters) {

  }
}
