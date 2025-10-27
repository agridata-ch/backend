package ch.agridata.agreement.persistence;

import ch.agridata.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Represents the association between a data request and its requested data products. It persists the linkage in the database schema.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Entity
@Table(name = "data_request_data_product")
@SQLDelete(sql = "UPDATE data_request_data_product SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataRequestDataProductEntity extends AuditableEntity {
  @Id
  @Column(name = "id", nullable = false, updatable = false)
  @GeneratedValue
  // we could use a combined primary key with data_request_id and data_product_id, but this does not allow for soft delete
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "data_request_id", nullable = false)
  private DataRequestEntity dataRequest;

  @Column(name = "data_product_id", nullable = false, updatable = false)
  private UUID dataProductId;

  public DataRequestDataProductEntity(DataRequestEntity dataRequest, UUID dataProductId) {
    this.dataRequest = dataRequest;
    this.dataProductId = dataProductId;
  }

}
