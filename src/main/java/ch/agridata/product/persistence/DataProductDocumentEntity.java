package ch.agridata.product.persistence;

import ch.agridata.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * This entity represents a data product pdf-document. It contains the original filename and the status of the virus scan.
 *
 * @CommentLastReviewed 2026-07-09
 */

@Entity
@Table(name = "data_product_document",
    indexes = {
        @Index(name = "idx_data_product_document_data_product_id", columnList = "data_product_id")
    })
@SQLDelete(sql = "UPDATE data_product_document SET archived = true WHERE id = ?")
@SQLRestriction("archived = false")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataProductDocumentEntity extends AuditableEntity {
  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "data_product_id", nullable = false)
  private DataProductEntity dataProduct;

  @Column(name = "original_filename", length = 1024, nullable = false)
  private String originalFilename;

  @Column(name = "size_bytes", nullable = false)
  private Long sizeBytes;

  @Column(name = "scan_status", nullable = false)
  @Enumerated(EnumType.STRING)
  private DocumentScanStatusEnum scanStatus;

}
