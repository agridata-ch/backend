package ch.agridata.agreement.persistence;

import java.time.LocalDateTime;

/**
 * Read-only projection of {@link ConsentRequestEntity} containing only the columns needed for the CSV export. Avoids loading and
 * tracking full managed entities when exporting large numbers of consent requests.
 *
 * @CommentLastReviewed 2026-10-09
 */
public record ConsentRequestExportProjection(
    String dataProducerUid,
    String dataProducerBur,
    LocalDateTime requestDate,
    LocalDateTime lastStateChangeDate,
    ConsentRequestEntity.StateEnum stateCode
) {
}
