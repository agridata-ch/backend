package ch.agridata.agreement.service;

import ch.agridata.agreement.persistence.OtpChallengeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Persists OTP challenge attempt-count increments in a transaction independent of the surrounding
 * signing transaction. The dedicated bean exists so the {@code REQUIRES_NEW} boundary is honoured by
 * the CDI proxy — calling an annotated method via {@code this} would silently inherit the caller's
 * transaction. The increment is performed as an atomic SQL UPDATE so concurrent verify calls cannot
 * lose increments via a read-modify-write race.
 *
 * @CommentLastReviewed 2026-05-08
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class OtpChallengeAttemptRecorder {

  private final OtpChallengeRepository otpChallengeRepository;

  @Transactional(value = TxType.REQUIRES_NEW)
  public void incrementAttemptCount(UUID challengeId) {
    int updated = otpChallengeRepository.update("attemptCount = attemptCount + 1 where id = ?1", challengeId);
    if (updated == 0) {
      log.warn("OTP challenge {} disappeared before attempt-count increment", challengeId);
    }
  }
}
