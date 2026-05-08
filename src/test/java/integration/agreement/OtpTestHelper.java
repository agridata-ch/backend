package integration.agreement;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class OtpTestHelper {

  public static final String FIXED_OTP_CODE = "123456";

  private final EntityManager entityManager;

  @Transactional
  public void overrideOtpHashForCode(UUID challengeId, String otpCode) {
    entityManager.createNativeQuery("UPDATE otp_challenge SET otp_hash = :hash WHERE id = :id")
        .setParameter("hash", sha256Hex(otpCode))
        .setParameter("id", challengeId)
        .executeUpdate();
  }

  @Transactional
  public void consumeChallenge(UUID challengeId) {
    entityManager.createNativeQuery("UPDATE otp_challenge SET consumed_at = :consumedAt WHERE id = :id")
        .setParameter("consumedAt", LocalDateTime.now())
        .setParameter("id", challengeId)
        .executeUpdate();
  }

  @Transactional
  public void expireChallenge(UUID challengeId) {
    entityManager.createNativeQuery("UPDATE otp_challenge SET expires_at = :expiresAt WHERE id = :id")
        .setParameter("expiresAt", LocalDateTime.now().minusMinutes(1))
        .setParameter("id", challengeId)
        .executeUpdate();
  }

  @Transactional
  public void backdateCreatedAtBeyondCooldown(UUID challengeId) {
    entityManager.createNativeQuery("UPDATE otp_challenge SET created_at = :createdAt WHERE id = :id")
        .setParameter("createdAt", LocalDateTime.now().minusMinutes(1))
        .setParameter("id", challengeId)
        .executeUpdate();
  }

  public static String sha256Hex(String code) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(code.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }
}
