package ch.agridata.agreement.service;

import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.persistence.OtpChallengeEntity;
import ch.agridata.agreement.persistence.OtpChallengeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages OTP challenge generation and verification.
 *
 * @CommentLastReviewed: 2026-03-19
 */

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class OtpChallengeService {
  private static final Duration OTP_TTL = Duration.ofMinutes(15);
  private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(30);
  private static final int MAX_ATTEMPTS = 1;

  private final OtpChallengeRepository otpChallengeRepository;
  private final Clock clock;
  private final Random random = new SecureRandom();

  @Transactional
  public OtpChallengeEntity createChallenge(
      UUID userId,
      UUID contractRevisionId,
      SignatureSlotCodeEnum signatureSlotCode,
      String phoneNumber
  ) {
    LocalDateTime now = LocalDateTime.now(clock);
    LocalDateTime cooldownThreshold = now.minus(RESEND_COOLDOWN);

    boolean existsRecentChallenge = otpChallengeRepository.existsRecentChallenge(
        userId, contractRevisionId, toEntitySignatureSlotEnum(signatureSlotCode), cooldownThreshold
    );

    if (existsRecentChallenge) {
      throw new ValidationException("OTP was sent too recently. Please wait " + RESEND_COOLDOWN.toSeconds()
          + " seconds before requesting another code.");
    }

    String otpCode = generateOtp();
    OtpChallengeEntity entity = OtpChallengeEntity.builder()
        .userId(userId)
        .contractRevisionId(contractRevisionId)
        .signatureSlotCode(toEntitySignatureSlotEnum(signatureSlotCode))
        .otpHash(hashOtp(otpCode))
        .phoneNumberSnapshot(phoneNumber)
        .expiresAt(now.plus(OTP_TTL))
        .maxAttempts(MAX_ATTEMPTS)
        .attemptCount(0)
        .build();

    otpChallengeRepository.persist(entity);

    // TODO: Implement sending out sms as soon as we have a way to do that

    return entity;
  }

  public OtpChallengeEntity.SignatureSlotCodeEnum toEntitySignatureSlotEnum(SignatureSlotCodeEnum signatureSlotCodeEnum) {
    return OtpChallengeEntity.SignatureSlotCodeEnum.valueOf(signatureSlotCodeEnum.name());
  }

  @Transactional
  public void verifyAndConsume(
      UUID challengeId,
      UUID expectedUserId,
      UUID expectedContractRevisionId,
      SignatureSlotCodeEnum expectedSignatureSlotCode,
      String otpCode
  ) {
    LocalDateTime now = LocalDateTime.now(clock);
    OtpChallengeEntity entity = otpChallengeRepository.findActiveById(challengeId, now)
        .orElseThrow(() -> new ValidationException("Invalid or expired OTP code."));

    if (!entity.getUserId().equals(expectedUserId)
        || !entity.getContractRevisionId().equals(expectedContractRevisionId)
        || !entity.getSignatureSlotCode().equals(toEntitySignatureSlotEnum(expectedSignatureSlotCode))) {
      throw new ValidationException("Invalid OTP challenge.");
    }

    entity.setAttemptCount(entity.getAttemptCount() + 1);
    if (entity.getAttemptCount() > entity.getMaxAttempts()) {
      throw new ValidationException("Too many attempts. Please request a new code.");
    }

    // TODO: Check if the OTP code is correct as soon as we can send it out.

    entity.setConsumedAt(now);
  }

  public String generateOtp() {
    return String.format("%06d", random.nextInt(999999));
  }

  public Duration getResendCooldown() {
    return RESEND_COOLDOWN;
  }


  private String hashOtp(String otpCode) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] encodedHash = digest.digest(otpCode.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(encodedHash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Error hashing OTP code", e);
    }
  }
}
