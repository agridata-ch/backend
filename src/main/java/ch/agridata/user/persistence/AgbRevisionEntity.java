package ch.agridata.user.persistence;

import ch.agridata.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
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
 * Entity representing an agb revision. It is used to store the revision of the agb during a specified date range
 *
 * @CommentLastReviewed 2026-07-16
 */

@Entity
@Table(name = "agb_revision")
@SQLDelete(sql = "UPDATE agb_revision SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgbRevisionEntity extends AuditableEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "valid_from")
  private LocalDateTime validFrom;

  @Column(name = "valid_to")
  private LocalDateTime validTo;

  @Column(name = "enforce_consent_from")
  private LocalDateTime enforceConsentFrom;

  @Column(name = "version", length = 20)
  private String version;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "agb_text")
  @Valid
  private AgbTextDto agbText;
}
