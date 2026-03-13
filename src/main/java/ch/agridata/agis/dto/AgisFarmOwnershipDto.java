package ch.agridata.agis.dto;

/**
 * DTO representing the ownership of a farm as returned by AGIS.
 *
 * <p>It contains:
 * <ul>
 *   <li><b>BUR</b> — the unique identifier of the farm.</li>
 *   <li><b>UID</b> — the unique identifier of the legal entity currently
 *       owning the farm.</li>
 * </ul>
 *
 * <p>This pairing is primarily used to detect ownership changes by comparing
 * the current AGIS state with stored consent request data.
 *
 * @CommentLastReviewed 2026-02-23
 */

public record AgisFarmOwnershipDto(
    String bur,
    String uid
) {
}
