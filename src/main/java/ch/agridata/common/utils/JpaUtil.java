package ch.agridata.common.utils;

import com.google.common.collect.Collections2;
import io.quarkus.panache.common.Sort;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

/**
 * Utility class for JPA-related operations, such as building dynamic queries and parsing sort parameters.
 *
 * @CommentLastReviewed 2025-09-11
 */
@UtilityClass
public class JpaUtil {

  private static final String PARAM_PREFIX = "param";
  public static final String PARAM_FULL_SEARCH_STRING = "paramfull";
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
   * Create WHERE clause for a SQL query that checks if any of the specified fields contains
   * the searchTerm string or any of its tokens.
   * The search is case-insensitive and uses the SQL LIKE operator with wildcards.
   *
   * @param searchTerm     The searchTerm string to search for.
   * @param fields         The list of fields to search in.
   * @param combinedFields A list of lists, where each sublist contains fields that should be combined
   *                       in a single WHERE clause using AND.
   * @return A Where clause.
   */
  public static WhereClause createContainsWhereClause(String searchTerm, List<String> fields, List<List<String>> combinedFields) {
    if (searchTerm == null) {
      return new WhereClause(DUMMY_WHERE_CLAUSE, Map.of());
    }
    String cleanedInput = cleanSearchString(searchTerm);
    var parameters = createParametersFromSearchTermTokens(cleanedInput);
    var tokenParameterNames = parameters.keySet().stream().filter(parameter -> !parameter.equals(PARAM_FULL_SEARCH_STRING)).toList();

    var simpleWhereClauses = fields.stream()
        .map(field ->
            createWhereClause(field, PARAM_FULL_SEARCH_STRING)
        );

    var combinedWhereClauses = combinedFields.stream()
        .flatMap(subset -> createWhereClauseForPermutationOfCombinedFields(subset, tokenParameterNames));

    var whereClause = Stream.of(simpleWhereClauses, combinedWhereClauses)
        .flatMap(s -> s)
        .filter(Objects::nonNull)
        .collect(Collectors.joining(" OR "));
    if (whereClause.isEmpty()) {
      return createDummyWhereClause();
    }
    return new WhereClause(whereClause, parameters);

  }

  private static Stream<String> createWhereClauseForPermutationOfCombinedFields(List<String> fields, List<String> parameterNames) {
    return Collections2.permutations(fields).stream().map(
        perm -> createWhereClauseForCombinedFields(perm, parameterNames)
    ).filter(Objects::nonNull);
  }

  private static String createWhereClauseForCombinedFields(List<String> fields, List<String> parameterNames) {
    if (fields == null || fields.isEmpty() || parameterNames == null || parameterNames.isEmpty() || fields.size() > parameterNames.size()) {
      return null;
    }
    var combinationWhereClauses = new ArrayList<String>();
    for (var i = 0; i < fields.size(); i++) {
      combinationWhereClauses.add(createWhereClause(fields.get(i), parameterNames.get(i)));
    }
    return String.join(" AND ", combinationWhereClauses);
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

  private Map<String, Object> createParametersFromSearchTermTokens(String input) {
    var parameters = new HashMap<String, Object>();

    if (input == null) {
      return parameters;
    }

    String cleanedInput = cleanSearchString(input);
    parameters.put(PARAM_FULL_SEARCH_STRING, createContainsValue(cleanedInput));

    List<String> tokens = List.of(input.split("\\s+"));

    if (tokens.size() > 1) {
      for (int i = 0; i < tokens.size(); i++) {
        parameters.put(PARAM_PREFIX + i, createContainsValue(tokens.get(i)));
      }
    }

    return parameters;
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
