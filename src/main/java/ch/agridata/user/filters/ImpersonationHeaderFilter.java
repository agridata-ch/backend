package ch.agridata.user.filters;

import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;

import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.user.service.UserService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.MDC;

/**
 * A JAX-RS filter that processes the "X-Impersonated-AgateLoginId" header for impersonation purposes.
 * This filter checks for the presence of the impersonation header in incoming requests.
 * If present and the current user has support privileges, it sets the impersonated KtIdP
 * in the security identity and adds it to the MDC for logging. The MDC entry is removed
 * after the request is processed to avoid leakage between requests.
 *
 * @CommentLastReviewed 2025-12-31
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class ImpersonationHeaderFilter implements ContainerRequestFilter, ContainerResponseFilter {
  public static final String IMPERSONATION_HEADER = "X-Impersonated-AgateLoginId";
  private static final String IMPERSONATION_MDC_FIELD = "userId";
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final UserService userService;

  @Inject
  public ImpersonationHeaderFilter(AgridataSecurityIdentity agridataSecurityIdentity,
                                   UserService userService) {
    this.agridataSecurityIdentity = agridataSecurityIdentity;
    this.userService = userService;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    if (!agridataSecurityIdentity.isSupport()) {
      resetImpersonation();
      return;
    }

    String impersonatedAgateLoginId = requestContext.getHeaderString(IMPERSONATION_HEADER);

    if (impersonatedAgateLoginId == null) {
      resetImpersonation();
      return;
    }

    var userInfo = userService.getUserInfo(impersonatedAgateLoginId);

    if (!userInfo.rolesAtLastLogin().contains(PRODUCER_ROLE)) {
      resetImpersonation();
      throw new IllegalArgumentException(
          "Not allowed to impersonate agateLoginId=" + impersonatedAgateLoginId
              + ". Only producer users can be impersonated");
    }

    MDC.put(IMPERSONATION_MDC_FIELD, userInfo.agateLoginId());
    agridataSecurityIdentity.setImpersonatedAgateLoginId(userInfo.agateLoginId());
    agridataSecurityIdentity.setImpersonatedKtIdP(userInfo.ktIdP());
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    MDC.remove(IMPERSONATION_MDC_FIELD);
  }

  private void resetImpersonation() {
    agridataSecurityIdentity.setImpersonatedAgateLoginId(null);
    agridataSecurityIdentity.setImpersonatedKtIdP(null);
  }
}

