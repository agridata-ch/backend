package ch.agridata.common.filters;

import static ch.agridata.common.security.AgridataSecurityIdentity.ACCESS_TOKEN_CLAIM_AGATE_LOGIN_ID;
import static ch.agridata.common.security.AgridataSecurityIdentity.AGRIDATA_AGATE_LOGIN_NAMESPACE;

import com.fasterxml.uuid.Generators;
import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.impl.jose.JWT;
import io.vertx.ext.web.RoutingContext;
import java.util.UUID;
import org.jboss.logging.MDC;

/**
 * Adds request identifiers and API paths to the logging context before security checks. It improves traceability for early request stages.
 *
 * @CommentLastReviewed 2025-08-25
 */
public class PreSecurityMdcFilter {

  public static final String REQUEST_ID_MDC_FIELD = "requestId";
  public static final String API_MDC_FIELD = "api";
  private static final String USER_ID_MDC_FIELD = "agateUserId";

  // Higher numbers = higher priority (runs earlier)
  @RouteFilter(1501)
  void beforeSecurity(RoutingContext ctx) {
    MDC.put(REQUEST_ID_MDC_FIELD, UUID.randomUUID().toString());
    MDC.put(USER_ID_MDC_FIELD, getUserId(ctx));
    String path = ctx.request().path();
    MDC.put(API_MDC_FIELD, path);

    ctx.next();
  }

  private String getUserId(RoutingContext context) {
    String authHeader = context.request().getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      JsonObject jwtData = JWT.parse(token);
      return generateUserIdFromToken(jwtData);
    }
    return "anonymous";
  }

  private String generateUserIdFromToken(JsonObject jwtData) {
    JsonObject payload = jwtData.getJsonObject("payload");

    if (payload != null && payload.getString(ACCESS_TOKEN_CLAIM_AGATE_LOGIN_ID) != null) {
      var id = payload.getString(ACCESS_TOKEN_CLAIM_AGATE_LOGIN_ID);
      return Generators.nameBasedGenerator(AGRIDATA_AGATE_LOGIN_NAMESPACE).generate(id).toString();
    }
    return "unknown";
  }
}
