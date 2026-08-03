package ch.agridata.product.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides persistence operations for DataProductDocumentEntity.
 *
 * @CommentLastReviewed 2026-07-09
 */

@ApplicationScoped
public class DataProductDocumentRepository implements PanacheRepositoryBase<DataProductDocumentEntity, UUID> {
  public List<DataProductDocumentEntity> findByDataProductId(UUID dataProductId) {
    return find("dataProduct.id", dataProductId).list();
  }

  public List<DataProductDocumentEntity> findByDataProductIdAndScanStatus(UUID dataProductId, DocumentScanStatusEnum scanStatus) {
    return find("dataProduct.id = ?1 and scanStatus = ?2", dataProductId, scanStatus).list();
  }

  public Optional<DataProductDocumentEntity> findByDataProductIdAndDocumentId(UUID dataProductId, UUID documentId) {
    return find("dataProduct.id = ?1 and id = ?2", dataProductId, documentId).firstResultOptional();
  }

  public boolean existsByDataProductIdAndScanStatusNot(UUID dataProductId, DocumentScanStatusEnum scanStatus) {
    return count("dataProduct.id = :dataProductId and scanStatus != :scanStatus",
        Map.of("dataProductId", dataProductId, "scanStatus", scanStatus)) > 0;
  }

  /**
   * Loads a document by id while acquiring a pessimistic write lock on its row, so a scan-status
   * transition cannot race a concurrent writer (the in-process poll vs. the recovery watchdog).
   */
  public Optional<DataProductDocumentEntity> findByIdForUpdate(UUID id) {
    return Optional.ofNullable(findById(id, LockModeType.PESSIMISTIC_WRITE));
  }

  /**
   * Finds documents still awaiting a scan result whose creation timestamp is older than the given
   * cutoff. Used by the scan recovery watchdog to detect documents whose in-process poller never
   * completed (e.g. a dead virtual thread or a crashed container).
   */
  public List<DataProductDocumentEntity> findStalePendingScans(LocalDateTime cutoff) {
    return find("scanStatus = ?1 and createdAt < ?2", DocumentScanStatusEnum.PENDING_SCAN, cutoff).list();
  }
}
