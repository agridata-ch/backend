package ch.agridata.common.persistence;

import java.util.UUID;
import java.util.function.Function;

/**
 * Describes a column that can be filtered on in a generic paged query. A filter always matches
 * exactly; the value parser converts the raw request value into the type of the mapped column.
 *
 * @param path        The HQL path of the column, e.g. {@code "ds.id"}.
 * @param valueParser Converts a raw request value into the column's type; throws for values that
 *                    cannot be converted.
 * @CommentLastReviewed 2026-09-14
 */
public record FilterField(String path, Function<String, Object> valueParser) {

  /**
   * Filter on a text column, matching the raw request value exactly. {@code ,} and {@code ;} separate
   * the values and filters of a request, so a value containing either cannot be expressed.
   */
  public static FilterField text(String path) {
    return new FilterField(path, value -> value);
  }

  public static FilterField uuid(String path) {
    return new FilterField(path, UUID::fromString);
  }

  Object parse(String value) {
    try {
      return valueParser.apply(value);
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Invalid filter value", e);
    }
  }
}
