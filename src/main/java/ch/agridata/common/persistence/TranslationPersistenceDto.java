package ch.agridata.common.persistence;

import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Models translation content for persistence. It enables storage and retrieval of multilingual values.
 *
 * <p>The {@link #MAX_LENGTH} ceiling is a persistence-level safety net that applies uniformly to every multilingual field. The precise,
 * field-specific business limits remain declared on the respective API DTOs.
 *
 * @CommentLastReviewed 2026-09-02
 */
@Builder
public record TranslationPersistenceDto(@Size(max = MAX_LENGTH) String de, @Size(max = MAX_LENGTH) String fr,
                                        @Size(max = MAX_LENGTH) String it) {

  /**
   * Maximum length enforced per language at persistence level.
   */
  public static final int MAX_LENGTH = 10000;

}
