package ch.agridata.common.persistence;

import ch.agridata.common.dto.SupportedLanguage;

/**
 * Describes a field usable in generic search, filter and sort queries. A field is either a plain
 * single-language column ({@link #simple(String)}) or a multilingual JSON column holding a
 * {@link TranslationPersistenceDto}, resolved against the request language ({@link #translated(String)}).
 *
 * @CommentLastReviewed 2026-07-09
 */
public record SearchField(String path, boolean translated) {

  public static SearchField simple(String path) {
    return new SearchField(path, false);
  }

  public static SearchField translated(String path) {
    return new SearchField(path, true);
  }

  /**
   * Renders this field as an HQL expression. Translated fields are resolved to the JSON value of
   * the given request language; the language segment comes exclusively from the
   * {@link SupportedLanguage} enum and is therefore safe to interpolate.
   *
   * <p>Uses PostgreSQL's {@code jsonb_extract_path_text} instead of the standard HQL
   * {@code json_value()}, which is still tech preview in Hibernate ORM 7.4 (would require the
   * {@code hibernate.query.hql.json_functions_enabled} opt-in).
   */
  String toHql(SupportedLanguage language) {
    if (!translated) {
      return path;
    }
    return "function('jsonb_extract_path_text', %s, '%s')".formatted(path, language.code());
  }
}
