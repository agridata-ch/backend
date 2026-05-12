package ch.agridata.common.dto;

/**
 * Defines the set of supported languages for the application.
 *
 * @CommentLastReviewed 2026-05-18
 */
public enum SupportedLanguage {
  DE, FR, IT;

  public static SupportedLanguage from(String value) {
    return switch (value.toLowerCase()) {
      case "fr" -> FR;
      case "it" -> IT;
      default -> DE;
    };
  }

  public String code() {
    return name().toLowerCase();
  }
}
