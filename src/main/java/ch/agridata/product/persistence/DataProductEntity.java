package ch.agridata.product.persistence;

import ch.agridata.common.persistence.AuditableEntity;
import ch.agridata.common.persistence.TranslationPersistenceDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
 * Declares operations for retrieving a product or its provider configuration by identifier. It ensures a stable contract for other
 * modules.
 *
 * @CommentLastReviewed 2025-08-25
 */
@Entity
@Table(name = "data_product",
    indexes = {
        @Index(name = "idx_data_product_data_source_system_code", columnList = "data_source_system_code")
    })
@SQLDelete(sql = "UPDATE data_product SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataProductEntity extends AuditableEntity {
  @Id
  @Column(name = "id", nullable = false, updatable = false)
  @GeneratedValue
  private UUID id;

  @Column(name = "data_source_system_code")
  @Enumerated(EnumType.STRING)
  private DataSourceSystemEnum dataSourceSystemCode;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "name")
  private TranslationPersistenceDto name;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "description")
  private TranslationPersistenceDto description;

  @Column(name = "rest_client_identifier_code", length = 50)
  private String restClientIdentifierCode;

  @Column(name = "rest_client_method_code", length = 50)
  private String restClientMethodCode;

  @Column(name = "rest_client_path", length = 1000)
  private String restClientPath;

  @Column(name = "rest_client_request_template", length = 1000)
  private String restClientRequestTemplate;

  /**
   * Enumerates supported external systems that serve as sources for data products.
   *
   * @CommentLastReviewed 2025-08-25
   */
  public enum DataSourceSystemEnum {
    AGIS
  }
}


