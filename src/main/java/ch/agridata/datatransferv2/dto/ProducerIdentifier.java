package ch.agridata.datatransferv2.dto;

import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Identifies a data producer. The UID is always present. The BUR is only set when the producer
 * must additionally be identified by their BUR number.
 *
 * @CommentLastReviewed 2026-03-18
 */
@Builder
public record ProducerIdentifier(
    @Schema(
        description = "Producer UID",
        examples = {"CHE123456789"}
    )
    String uid,
    @Schema(
        description = "Producer BUR, only set when the producer must additionally be identified by BUR.",
        examples = {"A12345678"},
        nullable = true
    )
    String bur
) {
}
