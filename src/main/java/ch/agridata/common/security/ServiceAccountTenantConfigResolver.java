package ch.agridata.common.security;

import io.quarkus.oidc.OidcRequestContext;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.TenantConfigResolver;
import io.quarkus.oidc.runtime.OidcConfig;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.impl.jose.JWT;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Resolves a dedicated OIDC tenant for service account tokens (client credentials grant). The tenant is a copy of the default tenant
 * with UserInfo disabled: service accounts have no personal data, and their tokens usually lack the {@code openid} scope, so Agate
 * rejects the UserInfo request and authentication would fail with 401.
 *
 * <p>The token is parsed without verification here; this only selects the configuration. Signature, issuer and audience are still
 * verified by the resolved tenant.
 *
 * @CommentLastReviewed 2026-10-08
 */
@ApplicationScoped
public class ServiceAccountTenantConfigResolver implements TenantConfigResolver {

  static final String SERVICE_ACCOUNT_TENANT_ID = "service-account";
  // Added by Keycloak's "service_account" client scope, absent in tokens of human users
  private static final String ACCESS_TOKEN_CLAIM_CLIENT_ID = "client_id";

  private final OidcTenantConfig serviceAccountTenantConfig;

  ServiceAccountTenantConfigResolver(OidcConfig oidcConfig) {
    this.serviceAccountTenantConfig = OidcTenantConfig.builder(OidcConfig.getDefaultTenant(oidcConfig))
        .tenantId(SERVICE_ACCOUNT_TENANT_ID)
        .authentication().userInfoRequired(false).end()
        .build();
  }

  @Override
  public Uni<OidcTenantConfig> resolve(RoutingContext routingContext, OidcRequestContext<OidcTenantConfig> requestContext) {
    return Uni.createFrom().item(isServiceAccountToken(routingContext) ? serviceAccountTenantConfig : null);
  }

  private boolean isServiceAccountToken(RoutingContext routingContext) {
    var authHeader = routingContext.request().getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return false;
    }
    try {
      JsonObject payload = JWT.parse(authHeader.substring(7)).getJsonObject("payload");
      return payload != null && payload.containsKey(ACCESS_TOKEN_CLAIM_CLIENT_ID);
    } catch (RuntimeException _) {
      // Malformed tokens are rejected by the default tenant
      return false;
    }
  }
}
