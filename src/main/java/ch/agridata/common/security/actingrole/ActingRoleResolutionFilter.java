package ch.agridata.common.security.actingrole;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;

/**
 * Resolves the effective acting role for each request hitting a JAX-RS method annotated with {@link EnableActingRoleHolder}.
 *
 * <p>Runs after authentication and {@code @RolesAllowed} enforcement so the filter only sees requests that have already
 * passed the coarse-grained role gate. The filter uses the <em>authenticated</em> user's roles via
 * {@link SecurityIdentity#getRoles()} — impersonation does NOT widen or change the role set used here. As a
 * consequence, a support user who impersonates a consumer but does not themselves hold the consumer role cannot
 * resolve to {@code actingRole=CONSUMER}.
 *
 * <p>The set of acceptable acting roles is <em>derived</em> from the method's (or its enclosing class's)
 * {@code @RolesAllowed} via {@link ActingRoleEnum#fromKeycloakRole(String)}. Roles that do not map to an
 * {@link ActingRoleEnum} value are skipped; ArchUnit guarantees that production code never lists such a role on a
 * {@code @EnableActingRoleHolder} method.
 *
 * @CommentLastReviewed 2026-05-26
 */
@Provider
@Priority(Priorities.AUTHORIZATION + 100)
@RequiredArgsConstructor
public class ActingRoleResolutionFilter implements ContainerRequestFilter {

  private static final String ACTING_ROLE_QUERY_PARAM = "actingRole";

  private final SecurityIdentity securityIdentity;
  private final ActingRoleHolder actingRoleHolder;

  @Context
  ResourceInfo resourceInfo;
  @Context
  UriInfo uriInfo;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    Method resourceMethod = resourceInfo.getResourceMethod();
    if (!filterEnabled(resourceMethod)) {
      return;
    }

    EnumSet<ActingRoleEnum> allowedRoles = allowedRolesFromRolesAllowed(resourceMethod);
    Optional<ActingRoleEnum> explicitActingRole = readActingRoleQueryParam();

    if (explicitActingRole.isPresent()) {
      applyExplicitActingRole(explicitActingRole.get(), allowedRoles);
      return;
    }

    autoResolveActingRole(allowedRoles);
  }

  private boolean filterEnabled(Method resourceMethod) {
    return resourceMethod != null && resourceMethod.getAnnotation(EnableActingRoleHolder.class) != null;
  }

  private void applyExplicitActingRole(ActingRoleEnum actingRole, EnumSet<ActingRoleEnum> allowedRoles) {
    if (!allowedRoles.contains(actingRole)) {
      throw new IllegalArgumentException(
          "actingRole=" + actingRole + " is not allowed for this endpoint. Allowed values: " + allowedRoles);
    }
    if (!userHasRole(actingRole)) {
      throw new ForbiddenException("The authenticated user does not have the role required for actingRole=" + actingRole);
    }
    actingRoleHolder.setRole(actingRole);
  }

  private void autoResolveActingRole(EnumSet<ActingRoleEnum> allowedRoles) {
    EnumSet<ActingRoleEnum> matchingRoles = intersectionWithUserRoles(allowedRoles);

    if (matchingRoles.size() == 1) {
      actingRoleHolder.setRole(matchingRoles.iterator().next());
      return;
    }
    if (matchingRoles.size() > 1) {
      throw new IllegalArgumentException(
          "actingRole query parameter is required because the authenticated user has multiple matching roles. Allowed values: "
              + matchingRoles);
    }
    throw new ForbiddenException("The authenticated user has none of the roles allowed for this endpoint: " + allowedRoles);
  }

  private boolean userHasRole(ActingRoleEnum actingRole) {
    return securityIdentity.getRoles().contains(actingRole.getKeycloakRole());
  }

  private EnumSet<ActingRoleEnum> allowedRolesFromRolesAllowed(Method method) {
    String[] keycloakRoles = readRolesAllowed(method);
    EnumSet<ActingRoleEnum> result = EnumSet.noneOf(ActingRoleEnum.class);
    for (String keycloakRole : keycloakRoles) {
      ActingRoleEnum.fromKeycloakRole(keycloakRole).ifPresent(result::add);
    }
    return result;
  }

  private Optional<ActingRoleEnum> readActingRoleQueryParam() {
    List<String> rawValues = uriInfo.getQueryParameters().get(ACTING_ROLE_QUERY_PARAM);
    String queryParam = (rawValues == null || rawValues.isEmpty()) ? null : rawValues.getFirst();

    if (queryParam == null || queryParam.isBlank()) {
      return Optional.empty();
    }

    try {
      return Optional.of(ActingRoleEnum.valueOf(queryParam));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("actingRole=" + queryParam + " is not a valid value", e);
    }
  }

  private String[] readRolesAllowed(Method method) {
    RolesAllowed methodLevel = method.getAnnotation(RolesAllowed.class);
    if (methodLevel != null) {
      return methodLevel.value();
    }
    RolesAllowed classLevel = method.getDeclaringClass().getAnnotation(RolesAllowed.class);
    if (classLevel != null) {
      return classLevel.value();
    }
    return new String[0];
  }

  private EnumSet<ActingRoleEnum> intersectionWithUserRoles(EnumSet<ActingRoleEnum> allowed) {
    Set<String> userRoles = securityIdentity.getRoles();
    EnumSet<ActingRoleEnum> result = EnumSet.noneOf(ActingRoleEnum.class);
    for (ActingRoleEnum role : allowed) {
      if (userRoles.contains(role.getKeycloakRole())) {
        result.add(role);
      }
    }
    return result;
  }
}
