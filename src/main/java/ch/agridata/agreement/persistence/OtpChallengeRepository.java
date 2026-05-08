package ch.agridata.agreement.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Manages persistence operations for OTP challenges. It allows querying OTP challenges by their ID and user ID.
 *
 * @CommentLastReviewed 2026-05-08
 */

@ApplicationScoped
public class OtpChallengeRepository implements PanacheRepositoryBase<OtpChallengeEntity, UUID> {

  public boolean existsRecentChallenge(UUID userId, UUID contractRevisionId, OtpChallengeEntity.SignatureSlotCodeEnum signatureSlotCode,
                                       LocalDateTime threshold) {
    return count(
        "userId = :userId "
            + "and contractRevisionId = :contractRevisionId "
            + "and signatureSlotCode = :signatureSlotCode "
            + "and createdAt > :threshold",
        Map.of(
            "userId", userId,
            "contractRevisionId", contractRevisionId,
            "signatureSlotCode", signatureSlotCode,
            "threshold", threshold
        )) > 0;
  }
}
