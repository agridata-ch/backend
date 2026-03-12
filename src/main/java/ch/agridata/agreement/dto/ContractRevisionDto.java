package ch.agridata.agreement.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

/**
 * Represents a complete contract revision.
 */

@Builder
public record ContractRevisionDto(
    @NotNull
    UUID id,

    @NotNull
    UUID dataRequestId,

    @NotNull
    LocalDateTime createdAt,

    @NotNull
    String dataConsumerName,

    @NotNull
    String dataProviderName

) {
}
