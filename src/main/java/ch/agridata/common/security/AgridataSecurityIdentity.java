package ch.agridata.common.security;

import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;

import ch.agridata.common.exceptions.UidMissingException;
import com.fasterxml.uuid.Generators;
import io.quarkus.oidc.UserInfo;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Provides access to user-related security claims. It extracts identifiers, email, and UID from security tokens and ensures validity.
 *
 * @CommentLastReviewed 2025-08-25
 */

@RequestScoped
@RequiredArgsConstructor
@Slf4j
public class AgridataSecurityIdentity {

  public static final String ACCESS_TOKEN_CLAIM_AGATE_LOGIN_ID = "loginid";
  public static final UUID AGRIDATA_AGATE_LOGIN_NAMESPACE =
      UUID.nameUUIDFromBytes("agridata-agate-login-id".getBytes(StandardCharsets.UTF_8));
  private static final String ACCESS_TOKEN_CLAIM_UID = "uid";


  private static final String ACCESS_TOKEN_CLAIM_KT_ID_P = "KT_ID_P";
  private static final String ACCESS_TOKEN_CLAIM_EMAIL = "email";

  private final SecurityIdentity securityIdentity;
  @Getter
  @Setter
  private String impersonatedKtIdP;

  public UUID getUserId() {
    String agateLoginId = getAgateLoginId();
    if (agateLoginId == null) {
      throw new IllegalStateException("No agateLoginId found");
    }
    return Generators.nameBasedGenerator(AGRIDATA_AGATE_LOGIN_NAMESPACE).generate(agateLoginId);
  }

  public String getAgateLoginId() {
    return extractClaim(securityIdentity, ACCESS_TOKEN_CLAIM_AGATE_LOGIN_ID);
  }

  public String getKtIdpOfUserOrImpersonatedUser() {
    return isSupport()
        ? impersonatedKtIdP
        : extractClaim(securityIdentity, ACCESS_TOKEN_CLAIM_KT_ID_P);
  }

  public String getEmail() {
    return extractClaim(securityIdentity, ACCESS_TOKEN_CLAIM_EMAIL);
  }

  public String getUidOrElseThrow() {
    return Optional.ofNullable(extractClaim(securityIdentity, ACCESS_TOKEN_CLAIM_UID))
        .orElseThrow(() -> new UidMissingException("User with agateLoginId " + getAgateLoginId() + " has no UID assigned"));
  }

  public UserInfo getUserInfoOrElseThrow() {
    UserInfo userInfo = securityIdentity.getAttribute("userinfo");
    if (userInfo == null) {
      throw new IllegalStateException("UserInfo of user with agateLoginId " + getAgateLoginId() + " not found");
    }
    return userInfo;
  }

  public boolean isAnonymous() {
    return securityIdentity.isAnonymous();
  }

  public boolean isImpersonating() {
    return impersonatedKtIdP != null;
  }

  public boolean isSupport() {
    return securityIdentity.hasRole(SUPPORT_ROLE);
  }

  private String extractClaim(SecurityIdentity securityIdentity, String claimName) {
    var principal = securityIdentity.getPrincipal();
    if (principal instanceof JsonWebToken jwt) {
      return jwt.getClaim(claimName);
    } else {
      return null;
    }
  }

}
