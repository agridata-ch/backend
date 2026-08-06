package ch.agridata.product.service;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;

import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.dto.DataProductDocumentMetadataDto;
import ch.agridata.product.dto.DocumentDownloadDto;
import ch.agridata.product.mapper.DataProductDocumentMapper;
import ch.agridata.product.persistence.DataProductDocumentEntity;
import ch.agridata.product.persistence.DataProductDocumentRepository;
import ch.agridata.product.persistence.DataProductRepository;
import ch.agridata.product.persistence.DocumentScanStatusEnum;
import io.quarkus.arc.Arc;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/**
 * CRUD orchestration for data product documents: authorize (via {@link DataProductAccessGuard}),
 * move bytes to/from S3, persist metadata, and map to DTOs. The GuardDuty scan lifecycle lives in
 * {@link DataProductDocumentScanService}; this class only triggers the scan polling via
 * identity-propagating async execution on virtual threads (see {@link #runAsyncAsUser}).
 *
 * @CommentLastReviewed 2026-07-10
 */

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DataProductDocumentService {

  @ConfigProperty(name = "agridata.product.documents.max-size-bytes", defaultValue = "10485760")
  long maxSizeBytes;

  private static final long LONG_POLL_TIMEOUT_MILLIS = 10_000;
  private static final long LONG_POLL_INTERVAL_MILLIS = 1_000;
  private static final long MAX_DOCUMENTS_PER_DATA_PRODUCT = 5;

  private final DataProductDocumentRepository dataProductDocumentRepository;
  private final DataProductDocumentMapper dataProductDocumentMapper;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final DataProductRepository dataProductRepository;
  private final DataProductAccessGuard dataProductAccessGuard;
  private final DataProductDocumentScanService dataProductDocumentScanService;
  private final PdfValidator pdfValidator;
  private final DataProductDocumentStorageService dataProductDocumentStorageService;
  private final ManagedExecutor managedExecutor;

  @RolesAllowed({PROVIDER_ROLE})
  public DataProductDocumentMetadataDto addDataProductDocumentAsProvider(UUID dataProductId, FileUpload fileUpload) {
    dataProductAccessGuard.verifyOwnedByCurrentProvider(dataProductId);
    return addDataProductDocument(dataProductId, fileUpload);
  }

  @RolesAllowed({ADMIN_ROLE})
  public DataProductDocumentMetadataDto addDataProductDocumentAsAdmin(UUID dataProductId, FileUpload fileUpload) {
    dataProductAccessGuard.verifyExists(dataProductId);
    return addDataProductDocument(dataProductId, fileUpload);
  }

  private DataProductDocumentMetadataDto addDataProductDocument(UUID dataProductId, FileUpload fileUpload) {

    if (fileUpload.size() > maxSizeBytes) {
      throw new ValidationException(
          "File exceeds the maximum size of " + maxSizeBytes + " bytes");
    }

    pdfValidator.validate(fileUpload.contentType(), fileUpload.uploadedFile());

    // Fail fast before paying for an S3 put if the product is already at its document limit.
    // The authoritative check runs again under a pessimistic lock below to close the race
    // between concurrent uploads to the same data product.
    verifyDocumentLimit(dataProductId);

    var documentId = UUID.randomUUID();

    dataProductDocumentStorageService.upload(documentId, readBytes(fileUpload));

    try {
      var dto = QuarkusTransaction.requiringNew().call(() -> {
        var dataProductEntity = dataProductRepository.findById(dataProductId, LockModeType.PESSIMISTIC_WRITE);
        verifyDocumentLimit(dataProductId);

        var documentEntity = DataProductDocumentEntity.builder()
            .id(documentId)
            .dataProduct(dataProductEntity)
            .originalFilename(fileUpload.fileName())
            .sizeBytes(fileUpload.size())
            .scanStatus(DocumentScanStatusEnum.PENDING_SCAN)
            .build();
        dataProductDocumentRepository.persist(documentEntity);
        return dataProductDocumentMapper.toDto(documentEntity);
      });
      // Capture the user ID while the request context is still active; the async poll runs later.
      runAsyncAsUser(
          agridataSecurityIdentity.getUserId(),
          () -> dataProductDocumentScanService.pollUntilScanned(documentId));
      return dto;
    } catch (Exception e) {
      safeDeleteOrphan(documentId);
      throw e;
    }
  }

  private void verifyDocumentLimit(UUID dataProductId) {
    long count = dataProductDocumentRepository.count("dataProduct.id", dataProductId);
    if (count >= MAX_DOCUMENTS_PER_DATA_PRODUCT) {
      throw new ValidationException(
          "A data product can have at most " + MAX_DOCUMENTS_PER_DATA_PRODUCT + " documents");
    }
  }

  /**
   * Captures the current user ID from the active HTTP request context and submits the given task
   * to the virtual thread executor. The task runs on a fresh virtual thread with a newly activated
   * request context, in which the captured user ID is set as {@code runAsUserId} on
   * {@link AgridataSecurityIdentity}. This allows the {@code AuditingEntityListener} to resolve
   * {@code modifiedBy} correctly even though the original request context is no longer active.
   * Exceptions thrown by the task are logged and not rethrown.
   */
  private void runAsyncAsUser(UUID userId, Runnable task) {
    managedExecutor.submit(() -> {
      var requestContext = Arc.container().requestContext();
      requestContext.activate();
      try {
        agridataSecurityIdentity.setRunAsUserId(userId);
        task.run();
      } catch (RuntimeException e) {
        log.error("Async task as user {} failed", userId, e);
      } finally {
        requestContext.deactivate();
      }
    });
  }


  private void safeDeleteOrphan(UUID documentId) {
    try {
      dataProductDocumentStorageService.delete(documentId);
    } catch (RuntimeException ex) {
      log.error("Failed to clean up orphaned S3 object for document {}: {}", documentId, ex.getMessage());
    }
  }

  private static byte[] readBytes(FileUpload fileUpload) {
    try {
      return Files.readAllBytes(fileUpload.uploadedFile());
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read uploaded file", e);
    }
  }

  @RolesAllowed({PROVIDER_ROLE})
  public DocumentDownloadDto getDataProductDocumentAsProvider(UUID dataProductId, UUID documentId) {
    dataProductAccessGuard.verifyOwnedByCurrentProvider(dataProductId);
    return getDataProductDocument(dataProductId, documentId);
  }

  @RolesAllowed({ADMIN_ROLE})
  public DocumentDownloadDto getDataProductDocumentAsAdmin(UUID dataProductId, UUID documentId) {
    dataProductAccessGuard.verifyExists(dataProductId);
    return getDataProductDocument(dataProductId, documentId);
  }

  private DocumentDownloadDto getDataProductDocument(UUID dataProductId, UUID documentId) {
    var entity = dataProductDocumentRepository.findByDataProductIdAndDocumentId(dataProductId, documentId)
        .orElseThrow(() -> new NotFoundException(documentId.toString()));

    if (entity.getScanStatus() != DocumentScanStatusEnum.AVAILABLE) {
      throw new ForbiddenException("Scan status is not AVAILABLE for document " + documentId);
    }

    byte[] document = dataProductDocumentStorageService.download(entity.getId());
    return new DocumentDownloadDto(entity.getOriginalFilename(), document);
  }

  @Transactional
  @RolesAllowed({PROVIDER_ROLE})
  public void deleteDataProductDocumentAsProvider(UUID dataProductId, UUID documentId) {
    dataProductAccessGuard.verifyOwnedByCurrentProvider(dataProductId);
    deleteDataProductDocument(dataProductId, documentId);
  }

  @Transactional
  @RolesAllowed({ADMIN_ROLE})
  public void deleteDataProductDocumentAsAdmin(UUID dataProductId, UUID documentId) {
    dataProductAccessGuard.verifyExists(dataProductId);
    deleteDataProductDocument(dataProductId, documentId);
  }

  @Transactional
  @RolesAllowed({PROVIDER_ROLE, ADMIN_ROLE})
  public void deleteAllDataProductDocuments(UUID dataProductId) {
    dataProductDocumentRepository.findByDataProductId(dataProductId).forEach(this::deleteDocument);
  }

  private void deleteDataProductDocument(UUID dataProductId, UUID documentId) {
    var entity = dataProductDocumentRepository.findByDataProductIdAndDocumentId(dataProductId, documentId)
        .orElseThrow(() -> new NotFoundException(documentId.toString()));
    deleteDocument(entity);
  }

  private void deleteDocument(DataProductDocumentEntity entity) {
    dataProductDocumentStorageService.delete(entity.getId());
    dataProductDocumentRepository.delete(entity);
  }

  @RolesAllowed({PROVIDER_ROLE})
  public List<DataProductDocumentMetadataDto> getDataProductDocumentsMetadataAsProvider(UUID dataProductId) {
    dataProductAccessGuard.verifyOwnedByCurrentProvider(dataProductId);
    return getDataProductDocumentsMetadata(dataProductId);
  }

  @RolesAllowed({ADMIN_ROLE})
  public List<DataProductDocumentMetadataDto> getDataProductDocumentsMetadataAsAdmin(UUID dataProductId) {
    dataProductAccessGuard.verifyExists(dataProductId);
    return getDataProductDocumentsMetadata(dataProductId);
  }

  private List<DataProductDocumentMetadataDto> getDataProductDocumentsMetadata(UUID dataProductId) {
    var entities = dataProductDocumentRepository.findByDataProductId(dataProductId);
    return entities.stream().map(dataProductDocumentMapper::toDto).toList();
  }

  @RolesAllowed({PROVIDER_ROLE})
  public DataProductDocumentMetadataDto getDataProductDocumentMetadataAsProvider(UUID dataProductId, UUID documentId, boolean longPolling) {
    dataProductAccessGuard.verifyOwnedByCurrentProvider(dataProductId);
    return getDataProductDocumentMetadata(dataProductId, documentId, longPolling);
  }

  @RolesAllowed({ADMIN_ROLE})
  public DataProductDocumentMetadataDto getDataProductDocumentMetadataAsAdmin(UUID dataProductId, UUID documentId, boolean longPolling) {
    dataProductAccessGuard.verifyExists(dataProductId);
    return getDataProductDocumentMetadata(dataProductId, documentId, longPolling);
  }

  private DataProductDocumentMetadataDto getDataProductDocumentMetadata(UUID dataProductId, UUID documentId, boolean longPolling) {
    long deadline = System.currentTimeMillis() + LONG_POLL_TIMEOUT_MILLIS;
    while (true) {
      // Read in a fresh transaction each iteration so the async scan status update is observed.
      var entity = QuarkusTransaction.requiringNew().call(() ->
          dataProductDocumentRepository.findByDataProductIdAndDocumentId(dataProductId, documentId)
              .orElseThrow(() -> new NotFoundException("Data product document not found")));
      if (!longPolling
          || entity.getScanStatus() != DocumentScanStatusEnum.PENDING_SCAN
          || System.currentTimeMillis() >= deadline) {
        return dataProductDocumentMapper.toDto(entity);
      }
      try {
        //noinspection BusyWait - intentional: virtual thread releases platform thread during sleep
        Thread.sleep(LONG_POLL_INTERVAL_MILLIS);
      } catch (InterruptedException _) {
        Thread.currentThread().interrupt();
        return dataProductDocumentMapper.toDto(entity);
      }
    }
  }
}
