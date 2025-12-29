package ch.agridata.agreement.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Provides a consumer-facing view of a consent request. It focuses on identifiers, producer references, and the state of the request.
 *
 * @CommentLastReviewed 2025-12-31
 * @deprecated Replaced by {@link ConsentRequestConsumerViewV2Dto}
 */
@Schema(description = "Data transfer object representing a consent request")
@Builder
@Deprecated(since = "1.5")
public record ConsentRequestConsumerViewDto(

    @Schema(
        description = "Unique identifier of the consent request",
        examples = {"3fa85f64-5717-4562-b3fc-2c963f66afa6"}
    )
    @NotNull
    UUID id,

    @Schema(
        description = "UID of the data producer",
        examples = {"CHE123456789"}
    )
    String dataProducerUid,

    ConsentRequestStateEnum stateCode

) {
}
