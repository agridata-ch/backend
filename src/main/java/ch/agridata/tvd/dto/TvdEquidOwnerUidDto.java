package ch.agridata.tvd.dto;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;

/**
 * DTO representing the response from the TVD Animal Tracing API for equid owner legal units.
 * Contains a list of legal unit records with identifying and naming information.
 *
 * @CommentLastReviewed 2025-12-29
 */
@Builder
public record TvdEquidOwnerUidDto(List<Data> data) {

  /**
   * DTO representing a single equid owner legal unit entry returned by the TVD API.
   * Provides a helper to derive a human-readable display name from available fields.
   *
   * @CommentLastReviewed 2025-12-29
   */
  @Builder
  public record Data(
      String uid,
      String correspondenceLanguage,
      String name,
      String nameAddOn1,
      String nameAddOn2,
      String firstName,
      String lastName
  ) {

    public String getDisplayName() {
      String displayName = Stream.of(firstName, lastName)
          .filter(string -> string != null && !string.isBlank())
          .map(String::trim)
          .collect(Collectors.joining(" "));

      if (!displayName.isEmpty()) {
        return displayName;
      }

      return Stream.of(name, nameAddOn1, nameAddOn2)
          .filter(string -> string != null && !string.isBlank())
          .map(String::trim)
          .collect(Collectors.joining(" "));
    }
  }
}
