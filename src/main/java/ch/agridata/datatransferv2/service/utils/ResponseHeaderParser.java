package ch.agridata.datatransferv2.service.utils;

import ch.agridata.common.exceptions.ExternalWebServiceException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Shared helpers for locating and validating values in data-provider response headers.
 * Header lookup is case-insensitive; values must consist of alphanumeric characters only.
 *
 * @CommentLastReviewed 2026-07-29
 */
public final class ResponseHeaderParser {

  private static final Pattern ALPHANUMERIC = Pattern.compile("[a-zA-Z0-9]+");

  private ResponseHeaderParser() {
  }

  /**
   * Returns the value of the given header, matched case-insensitively, if present.
   */
  public static Optional<String> findHeaderValue(final Map<String, String> headers, final String headerName) {
    return headers.entrySet().stream()
        .filter(e -> headerName.equalsIgnoreCase(e.getKey()))
        .map(Map.Entry::getValue)
        .findFirst();
  }

  /**
   * Whether the given value consists solely of alphanumeric characters.
   */
  public static boolean isAlphanumeric(final String value) {
    return ALPHANUMERIC.matcher(value).matches();
  }

  /**
   * Parses a comma-separated header value into its trimmed, non-blank entries, rejecting any entry
   * that is not purely alphanumeric.
   *
   * @throws ExternalWebServiceException if an entry contains a non-alphanumeric character
   */
  public static List<String> parseAlphanumericCsv(final String value, final String headerName) {
    List<String> entries = Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();
    for (String entry : entries) {
      if (!isAlphanumeric(entry)) {
        throw new ExternalWebServiceException(headerName + " header contains invalid value '" + entry
            + "': only alphanumeric characters are allowed (entries separated by commas)");
      }
    }
    return entries;
  }
}
