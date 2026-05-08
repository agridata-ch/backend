package ch.agridata.agreement.service;

import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.persistence.OtpChallengeEntity;
import ch.agridata.agreement.persistence.OtpChallengeRepository;
import ch.agridata.aws.api.SmsApi;
import ch.agridata.common.exceptions.OtpExpiredException;
import ch.agridata.common.exceptions.OtpInvalidException;
import ch.agridata.common.exceptions.OtpLockedException;
import ch.agridata.common.exceptions.OtpResendCooldownException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Manages OTP challenge generation and verification.
 *
 * @CommentLastReviewed 2026-05-08
 */

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class OtpChallengeService {

  static final String VALIDATION_ENABLED_PROPERTY = "agridata.agreement.otp.validation.enabled";

  private static final Duration OTP_TTL = Duration.ofMinutes(15);
  private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(30);
  private static final int MAX_ATTEMPTS = 3;

  private final SmsApi smsApi;
  private final OtpChallengeRepository otpChallengeRepository;
  private final OtpChallengeAttemptRecorder attemptRecorder;
  private final Clock clock;
  private final Random random = new SecureRandom();

  @ConfigProperty(name = VALIDATION_ENABLED_PROPERTY, defaultValue = "true")
  boolean validationEnabled;

  @Transactional
  public OtpChallengeEntity createChallenge(
      UUID userId,
      UUID contractRevisionId,
      SignatureSlotCodeEnum signatureSlotCode,
      String phoneNumber
  ) {
    LocalDateTime now = LocalDateTime.now(clock);

    if (validationEnabled) {
      LocalDateTime cooldownThreshold = now.minus(RESEND_COOLDOWN);
      boolean existsRecentChallenge = otpChallengeRepository.existsRecentChallenge(
          userId, contractRevisionId, toEntitySignatureSlotEnum(signatureSlotCode), cooldownThreshold
      );

      if (existsRecentChallenge) {
        throw new OtpResendCooldownException(RESEND_COOLDOWN.toSeconds());
      }
    } else {
      log.warn("OTP resend cooldown check skipped because {}=false (userId={}, contractRevisionId={}, slot={})",
          VALIDATION_ENABLED_PROPERTY, userId, contractRevisionId, signatureSlotCode);
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

    sendOtp(phoneNumber, otpCode);

    return entity;
  }

  private void sendOtp(String phoneNumber, String otpCode) {
    if (phoneNumber == null || phoneNumber.isBlank()) {
      throw new IllegalArgumentException("Phone number is required.");
    }
    smsApi.sendSms(phoneNumber, "Sicherheitscode / Code Sécuritaire / Codice di sicurezza: " + otpCode);
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
    Optional<OtpChallengeEntity> maybeEntity = otpChallengeRepository.findByIdOptional(challengeId);
    if (maybeEntity.isEmpty()) {
      throw new OtpInvalidException("Invalid OTP challenge.");
    }
    OtpChallengeEntity entity = maybeEntity.get();

    if (!entity.getUserId().equals(expectedUserId)
        || !entity.getContractRevisionId().equals(expectedContractRevisionId)
        || !entity.getSignatureSlotCode().equals(toEntitySignatureSlotEnum(expectedSignatureSlotCode))) {
      throw new OtpInvalidException("Invalid OTP challenge.");
    }

    if (!validationEnabled) {
      log.warn("OTP validation skipped because {}=false (challengeId={})", VALIDATION_ENABLED_PROPERTY, challengeId);
      entity.setConsumedAt(now);
      return;
    }

    if (entity.getConsumedAt() != null) {
      throw new OtpInvalidException("OTP challenge has already been consumed.");
    }
    if (!entity.getExpiresAt().isAfter(now)) {
      throw new OtpExpiredException("OTP challenge has expired.");
    }
    if (entity.getAttemptCount() >= entity.getMaxAttempts()) {
      throw new OtpLockedException("OTP challenge is locked after too many failed attempts.");
    }

    attemptRecorder.incrementAttemptCount(challengeId);

    if (!hashOtp(otpCode).equals(entity.getOtpHash())) {
      if (entity.getAttemptCount() + 1 >= entity.getMaxAttempts()) {
        throw new OtpLockedException("OTP challenge is locked after too many failed attempts.");
      }
      throw new OtpInvalidException("Invalid OTP code.");
    }

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
