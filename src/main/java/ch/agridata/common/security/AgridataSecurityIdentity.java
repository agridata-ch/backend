package ch.agridata.common.security;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;

import ch.agridata.common.exceptions.UidMissingException;
import com.fasterxml.uuid.Generators;
import io.quarkus.oidc.UserInfo;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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

  private final SecurityIdentity securityIdentity;

  @Setter
  private String impersonatedAgateLoginId;
  @Setter
  private String impersonatedKtIdP;

  public UUID getUserId() {
    String agateLoginId = getAgateLoginId();
    if (agateLoginId == null) {
      throw new IllegalStateException("No agateLoginId found");
    }
    return Generators.nameBasedGenerator(AGRIDATA_AGATE_LOGIN_NAMESPACE).generate(agateLoginId);
  }

  /**
   * Returns the Agate login ID of the currently authenticated user.
   *
   * <p>This method never returns an impersonated value. If support-user impersonation is enabled
   * and the current operation should run in the context of the impersonated user, use
   * {@link #getAgateLoginIdOrImpersonatedAgateLoginId()} instead.
   *
   * @return the Agate login ID of the authenticated user (from the access token claim)
   */
  public String getAgateLoginId() {
    return extractClaim(securityIdentity, ACCESS_TOKEN_CLAIM_AGATE_LOGIN_ID);
  }

  /**
   * Returns the Agate login ID for the effective user context.
   *
   * <p>By default, this is the Agate login ID of the authenticated user. If a support user is currently
   * impersonating another user, this method returns the Agate login ID of the impersonated user.
   *
   * <p>If you always need the Agate login ID of the authenticated user (ignoring impersonation), use
   * {@link #getAgateLoginId()}.
   *
   * @return the Agate login ID of the authenticated user, or of the impersonated user if impersonation is active
   */
  public String getAgateLoginIdOrImpersonatedAgateLoginId() {
    return isImpersonating() ? impersonatedAgateLoginId : getAgateLoginId();
  }

  /**
   * Returns the KT_ID_P of the currently authenticated user.
   *
   * <p>This method never returns an impersonated value. If support-user impersonation is enabled and the
   * current operation should run in the context of the impersonated user, use
   * {@link #getKtIdpOrImpersonatedKtIdP()} instead.
   *
   * @return the KT_ID_P of the authenticated user (from the access token claim)
   */
  public String getKtIdP() {
    return extractClaim(securityIdentity, ACCESS_TOKEN_CLAIM_KT_ID_P);
  }

  /**
   * Returns the KT_ID_P for the effective user context.
   *
   * <p>By default, this is the KT_ID_P of the authenticated user. If a support user is currently
   * impersonating another user, this method returns the KT_ID_P of the impersonated user.
   *
   * <p>If you always need the KT_ID_P of the authenticated user (ignoring impersonation), use
   * {@link #getKtIdP()}.
   *
   * @return the KT_ID_P of the authenticated user, or of the impersonated user if impersonation is active
   */
  public String getKtIdpOrImpersonatedKtIdP() {
    return isImpersonating() ? impersonatedKtIdP : getKtIdP();
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
    return impersonatedAgateLoginId != null;
  }

  public boolean isSupport() {
    return securityIdentity.hasRole(SUPPORT_ROLE);
  }

  public boolean isAdmin() {
    return securityIdentity.hasRole(ADMIN_ROLE);
  }

  public Set<String> getRoles() {
    return securityIdentity.getRoles();
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
