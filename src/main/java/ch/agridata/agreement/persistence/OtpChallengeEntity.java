package ch.agridata.agreement.persistence;

import ch.agridata.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Defines the persistent entity for OTP challenges.
 *
 * @CommentLastReviewed: 2026-03-19
 */

@Entity
@Table(name = "otp_challenge")
@SQLDelete(sql = "UPDATE otp_challenge SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpChallengeEntity extends AuditableEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "contract_revision_id", nullable = false)
  private UUID contractRevisionId;

  @Column(name = "signature_slot_code", nullable = false)
  @Enumerated(EnumType.STRING)
  private SignatureSlotCodeEnum signatureSlotCode;

  @Column(name = "otp_hash", nullable = false, length = 128)
  private String otpHash;

  @Column(name = "phone_number_snapshot", nullable = false, length = 50)
  private String phoneNumberSnapshot;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(name = "consumed_at")
  private LocalDateTime consumedAt;

  @Column(name = "attempt_count", nullable = false)
  private int attemptCount;

  @Column(name = "max_attempts", nullable = false)
  private int maxAttempts;

  /**
   * Lists the possible signature slots.
   *
   * @CommentLastReviewed: 2026-03-20
   */

  public enum SignatureSlotCodeEnum {
    DATA_CONSUMER_01,
    DATA_CONSUMER_02,
    DATA_PROVIDER_01,
    DATA_PROVIDER_02,
  }
}
