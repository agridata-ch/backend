package ch.agridata.user.dto;

import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Encapsulates an UID along with metadata such as name and legal form.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Schema(description = "Data transfer object representing an uid")
@Builder
public record UidDto(

    String uid,
    String name,
    LegalFormEnum legalFormCode

) {
}
