package ch.agridata.product.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO representing metadata of a data product document.
 *
 * @CommentLastReviewed 2026-07-09
 */

@Builder
public record DataProductDocumentMetadataDto(
    @Schema(
        description = "Unique identifier of the product",
        examples = {"3fa85f64-5717-4562-b3fc-2c963f66afb7"}
    )
    @NotNull
    UUID id,
    @Schema(
        description = "Filename of the document",
        examples = {"example.pdf"}
    )
    @NotNull
    String fileName,
    @Schema(
        description = "Filesize of the document",
        examples = {"1024"}
    )
    @NotNull
    long sizeBytes,
    @Schema(
        description = "Status of the document scan",
        examples = {"AVAILABLE"}
    )
    @NotNull
    DocumentScanStatusEnum scanStatus
) {
}
