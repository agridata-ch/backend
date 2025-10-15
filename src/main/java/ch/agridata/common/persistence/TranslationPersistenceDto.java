package ch.agridata.common.persistence;

import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Models translation content for persistence. It enables storage and retrieval of multilingual values.
 *
 * @CommentLastReviewed 2025-08-25
 */
@Builder
public record TranslationPersistenceDto(@Size(max = 4000) String de, @Size(max = 4000) String fr,
                                        @Size(max = 4000) String it) {

}
