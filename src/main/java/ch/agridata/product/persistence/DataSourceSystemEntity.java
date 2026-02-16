package ch.agridata.product.persistence;

import ch.agridata.common.persistence.AuditableEntity;
import ch.agridata.common.persistence.TranslationPersistenceDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
 * Defines the persistence representation of a data source system. It models a concrete technical
 * system operated by a data provider and acts as the grouping entity for related data products.
 *
 * @CommentLastReviewed 2026-02-06
 */

@Entity
@Table(name = "data_source_system",
    indexes = {
        @Index(name = "idx_data_source_system_data_provider_id", columnList = "data_provider_id")
    }
)
@SQLDelete(sql = "UPDATE data_source_system SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceSystemEntity extends AuditableEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "data_provider_id", nullable = false)
  private DataProviderEntity dataProvider;

  @Column(name = "code", length = 50, nullable = false)
  private String code;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "name")
  private TranslationPersistenceDto name;
}
