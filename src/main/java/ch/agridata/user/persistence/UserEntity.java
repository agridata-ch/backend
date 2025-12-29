package ch.agridata.user.persistence;

import ch.agridata.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

/**
 * Represents a persisted user in the system. It includes identifiers, login details, email, and optional metadata.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Entity
@Table(name = "users", //Using plural 'users' because 'user' is a reserved keyword in PostgreSQL
    indexes = {
        @Index(name = "idx_users_kt_id_p", columnList = "kt_id_p"),
        @Index(name = "idx_users_agate_login_id", columnList = "agate_login_id"),
        @Index(name = "idx_users_uid", columnList = "uid"),
    })
@SQLDelete(sql = "UPDATE user SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends AuditableEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "agate_login_id", length = 50)
  private String agateLoginId;

  @Column(name = "kt_id_p", length = 50)
  private String ktIdP;

  @Column(name = "uid", length = 20)
  private String uid;

  @Column(name = "email")
  private String email;

  @Column(name = "given_name", length = 500)
  private String givenName;

  @Column(name = "family_name", length = 500)
  private String familyName;

  @Column(name = "phone_number", length = 50)
  private String phoneNumber;

  @Column(name = "address_street", length = 500)
  private String addressStreet;

  @Column(name = "address_locality", length = 500)
  private String addressLocality;

  @Column(name = "address_postal_code", length = 50)
  private String addressPostalCode;

  @Column(name = "address_country", length = 50)
  private String addressCountry;

  @Column(name = "last_login_date")
  private LocalDateTime lastLoginDate;

  @Column(name = "user_preferences")
  @JdbcTypeCode(SqlTypes.JSON)
  private UserEntityPreferencesDto userPreferences;

  @Column(name = "roles_at_last_login")
  @JdbcTypeCode(SqlTypes.JSON)
  private Set<String> rolesAtLastLogin;
}
