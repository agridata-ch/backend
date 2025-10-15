package ch.agridata.common.filters;

import ch.agridata.common.security.AgridataSecurityIdentity;
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
 * A JAX-RS filter that processes the "X-Impersonated-KtIdP" header for impersonation purposes.
 * This filter checks for the presence of the impersonation header in incoming requests.
 * If present and the current user has support privileges, it sets the impersonated KtIdP
 * in the security identity and adds it to the MDC for logging. The MDC entry is removed
 * after the request is processed to avoid leakage between requests.
 *
 * @CommentLastReviewed 2025-10-02
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class ImpersonationHeaderFilter implements ContainerRequestFilter, ContainerResponseFilter {
  public static final String IMPERSONATION_HEADER = "X-Impersonated-KtIdP";
  private static final String IMPERSONATION_MDC_FIELD = "userId";
  private final AgridataSecurityIdentity agridataSecurityIdentity;

  @Inject
  public ImpersonationHeaderFilter(AgridataSecurityIdentity agridataSecurityIdentity) {
    this.agridataSecurityIdentity = agridataSecurityIdentity;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    if (!agridataSecurityIdentity.isSupport()) {
      agridataSecurityIdentity.setImpersonatedKtIdP(null);
      return;
    }

    String impersonatedKtIdP = requestContext.getHeaderString(IMPERSONATION_HEADER);
    if (impersonatedKtIdP != null) {
      MDC.put(IMPERSONATION_MDC_FIELD, impersonatedKtIdP);
    }

    agridataSecurityIdentity.setImpersonatedKtIdP(impersonatedKtIdP);
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    MDC.remove(IMPERSONATION_MDC_FIELD);
  }
}

