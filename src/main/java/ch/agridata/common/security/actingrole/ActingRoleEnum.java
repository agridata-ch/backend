package ch.agridata.common.security.actingrole;

import ch.agridata.common.utils.AuthenticationUtil;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumerates the roles a client can claim as the acting persona for a request via the {@code actingRole} query
 * parameter. Each value maps to exactly one Keycloak role string declared in {@link AuthenticationUtil} and is the
 * single source of truth for that mapping.
 *
 * @CommentLastReviewed 2026-05-20
 */
@RequiredArgsConstructor
@Getter
public enum ActingRoleEnum {
  CONSUMER(AuthenticationUtil.CONSUMER_ROLE),
  PROVIDER(AuthenticationUtil.PROVIDER_ROLE),
  PRODUCER(AuthenticationUtil.PRODUCER_ROLE),
  ADMIN(AuthenticationUtil.ADMIN_ROLE),
  SUPPORT(AuthenticationUtil.SUPPORT_ROLE);

  private static final Map<String, ActingRoleEnum> BY_KEYCLOAK_ROLE = Arrays.stream(values())
      .collect(Collectors.toUnmodifiableMap(ActingRoleEnum::getKeycloakRole, Function.identity()));

  private final String keycloakRole;

  public static Optional<ActingRoleEnum> fromKeycloakRole(String keycloakRole) {
    return Optional.ofNullable(BY_KEYCLOAK_ROLE.get(keycloakRole));
  }
}
