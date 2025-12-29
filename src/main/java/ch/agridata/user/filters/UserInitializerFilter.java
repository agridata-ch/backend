package ch.agridata.user.filters;

import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.user.persistence.UserEntity;
import ch.agridata.user.persistence.UserRepository;
import ch.agridata.user.service.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;

/**
 * Ensures that authenticated users are persisted in the system. It initializes user entities on first access, handling concurrent requests
 * safely with idempotent inserts.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Provider
@ApplicationScoped
@RequiredArgsConstructor
public class UserInitializerFilter implements ContainerRequestFilter {

  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final UserRepository userRepository;
  private final EntityManager entityManager;
  private final UserService userService;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    if (agridataSecurityIdentity.isAnonymous()) {
      return;
    }

    var existingUser = userRepository.findById(agridataSecurityIdentity.getUserId());
    if (existingUser != null) {
      return;
    }
    var newUser = UserEntity.builder()
        .id(agridataSecurityIdentity.getUserId())
        .agateLoginId(agridataSecurityIdentity.getAgateLoginId())
        .rolesAtLastLogin(agridataSecurityIdentity.getRoles())
        .build();

    persist(newUser);
    userService.updateUserData();
  }

  /**
   * Uses a native SQL query with PostgreSQL's "ON CONFLICT DO NOTHING" to ensure idempotent inserts. This avoids errors in case two
   * concurrent requests try to create the same user at the same time. If the user already exists (based on the primary key), the insert is
   * silently ignored.
   */
  @Transactional
  void persist(UserEntity user) {
    entityManager.createNativeQuery("""
            INSERT INTO users (id, archived, modified_at, created_at, created_by, modified_by, agate_login_id)
            VALUES (:id, :archived, :modifiedAt, :createdAt, :createdBy, :modifiedBy, :agateLoginId)
            ON CONFLICT (id) DO NOTHING
            """)
        .setParameter("id", user.getId())
        .setParameter("archived", false)
        .setParameter("modifiedAt", LocalDateTime.now())
        .setParameter("createdAt", LocalDateTime.now())
        .setParameter("createdBy", user.getId())
        .setParameter("modifiedBy", user.getId())
        .setParameter("agateLoginId", user.getAgateLoginId())
        .executeUpdate();
  }
}
