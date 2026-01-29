package ch.agridata.product.persistence;

import ch.agridata.common.persistence.AuditableEntity;
import ch.agridata.common.persistence.TranslationPersistenceDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
 * Defines the persistence representation of a data provider. It represents an owning organization
 * that supplies one or more data source systems and serves as a stable reference point for products
 * and configurations.
 *
 * @CommentLastReviewed 2026-02-06
 */

@Entity
@Table(name = "data_provider",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_data_provider_code", columnNames = "code")
    })
@SQLDelete(sql = "UPDATE data_provider SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataProviderEntity extends AuditableEntity {
  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  /**
   * Stable technical identifier used in configs and references.
   * Example: "BLW", "IDENTITAS", ...
   */
  @Column(name = "code", length = 50, nullable = false, unique = true)
  private String code;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "name")
  private TranslationPersistenceDto name;
}
