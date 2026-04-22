package ch.agridata.common.security;

import static ch.agridata.common.security.AgridataSecurityIdentity.ACCESS_TOKEN_CLAIM_AGATE_LOGIN_ID;
import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Set;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Augments the Quarkus {@link SecurityIdentity} with the producer role for equid owners.
 * Delegates the ownership check to {@link EquidOwnerChecker}. Skipped if the producer role
 * is already present in the token.
 *
 * @CommentLastReviewed 2026-03-12
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class RolesAugmentor implements SecurityIdentityAugmentor {

  private static final Set<String> AGRIDATA_ROLES = Set.of(PRODUCER_ROLE, CONSUMER_ROLE, PROVIDER_ROLE, ADMIN_ROLE, SUPPORT_ROLE);

  private final EquidOwnerChecker equidOwnerChecker;

  @Override
  public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
    return context.runBlocking(build(identity));
  }

  private Supplier<SecurityIdentity> build(SecurityIdentity identity) {
    if (identity.isAnonymous()) {
      return () -> identity;
    }
    return () -> {
      var agateLoginId = extractAgateLoginId(identity);
      if (agateLoginId == null || hasAnyAgridataRole(identity)) {
        return identity;
      }

      try {
        if (!equidOwnerChecker.isEquidOwner(agateLoginId)) {
          return identity;
        }
      } catch (Exception e) {
        log.error("EquidOwnerChecker {} failed for agateLoginId={}",
            equidOwnerChecker.getClass().getSimpleName(), agateLoginId, e);
        return identity;
      }

      return QuarkusSecurityIdentity.builder(identity)
          .addRole(PRODUCER_ROLE)
          .build();
    };
  }

  private boolean hasAnyAgridataRole(SecurityIdentity identity) {
    return AGRIDATA_ROLES.stream().anyMatch(identity::hasRole);
  }

  private String extractAgateLoginId(SecurityIdentity securityIdentity) {
    var principal = securityIdentity.getPrincipal();
    if (principal instanceof JsonWebToken jwt) {
      return jwt.getClaim(ACCESS_TOKEN_CLAIM_AGATE_LOGIN_ID);
    }
    return null;
  }
}
