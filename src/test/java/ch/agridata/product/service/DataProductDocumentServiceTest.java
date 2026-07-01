package ch.agridata.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.dto.DataProductDocumentMetadataDto;
import ch.agridata.product.mapper.DataProductDocumentMapper;
import ch.agridata.product.persistence.DataProductDocumentEntity;
import ch.agridata.product.persistence.DataProductDocumentRepository;
import ch.agridata.product.persistence.DataProductEntity;
import ch.agridata.product.persistence.DataProductRepository;
import ch.agridata.product.persistence.DocumentScanStatusEnum;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.narayana.jta.TransactionRunnerOptions;
import jakarta.persistence.LockModeType;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataProductDocumentServiceTest {
  @Mock
  DataProductDocumentRepository dataProductDocumentRepository;
  @Mock
  DataProductDocumentMapper dataProductDocumentMapper;
  @Mock
  AgridataSecurityIdentity agridataSecurityIdentity;
  @Mock
  DataProductRepository dataProductRepository;
  @Mock
  DataProductAccessGuard dataProductAccessGuard;
  @Mock
  DataProductDocumentScanService dataProductDocumentScanService;
  @Mock
  PdfValidator pdfValidator;
  @Mock
  DataProductDocumentStorageService dataProductDocumentStorageService;
  @Mock
  ManagedExecutor managedExecutor;
  @Mock
  FileUpload fileUpload;
  @Mock
  TransactionRunnerOptions transactionRunner;
  @InjectMocks
  DataProductDocumentService service;

  @TempDir
  Path tempDir;

  private static final UUID DATA_PRODUCT_ID = UUID.randomUUID();
  private static final UUID DOCUMENT_ID = UUID.randomUUID();
  private static final UUID USER_ID = UUID.randomUUID();
  private static final byte[] FILE_CONTENT = {1, 2, 3};

  @BeforeEach
  void setUp() {
    service.maxSizeBytes = 10_485_760L;
  }

  /**
   * Stubs a valid 3-byte PDF upload. Only called by tests that actually reach the upload path,
   * so strict stubbing stays happy for the download/delete/metadata tests.
   */
  private void stubValidUpload() throws Exception {
    Path uploadedFile = tempDir.resolve("document.pdf");
    Files.write(uploadedFile, FILE_CONTENT);
    when(fileUpload.size()).thenReturn(3L);
    when(fileUpload.contentType()).thenReturn("application/pdf");
    when(fileUpload.uploadedFile()).thenReturn(uploadedFile);
  }

  /**
   * Makes the mocked transaction runner actually execute the passed callable.
   */
  private void stubTransactionRunnerToInvokeCallable() {
    when(transactionRunner.call(any())).thenAnswer(invocation -> {
      Callable<?> callable = invocation.getArgument(0);
      return callable.call();
    });
  }

  private DataProductDocumentEntity documentEntity(DocumentScanStatusEnum scanStatus) {
    return DataProductDocumentEntity.builder()
        .id(DOCUMENT_ID)
        .originalFilename("document.pdf")
        .sizeBytes(3L)
        .scanStatus(scanStatus)
        .build();
  }

  @Nested
  class AddDocument {

    @Test
    void givenValidUpload_whenAddDocumentAsProvider_thenUploadsPersistsAndTriggersAsyncScan() throws Exception {
      stubValidUpload();
      when(fileUpload.fileName()).thenReturn("document.pdf");
      when(dataProductDocumentRepository.count("dataProduct.id", DATA_PRODUCT_ID)).thenReturn(0L);
      when(agridataSecurityIdentity.getUserId()).thenReturn(USER_ID);

      var dataProductEntity = new DataProductEntity();
      when(dataProductRepository.findById(DATA_PRODUCT_ID, LockModeType.PESSIMISTIC_WRITE))
          .thenReturn(dataProductEntity);

      var expectedDto = DataProductDocumentMetadataDto.builder().build();
      when(dataProductDocumentMapper.toDto(any(DataProductDocumentEntity.class))).thenReturn(expectedDto);

      try (MockedStatic<QuarkusTransaction> quarkusTransaction = mockStatic(QuarkusTransaction.class)) {
        quarkusTransaction.when(QuarkusTransaction::requiringNew).thenReturn(transactionRunner);
        stubTransactionRunnerToInvokeCallable();

        var result = service.addDataProductDocumentAsProvider(DATA_PRODUCT_ID, fileUpload);

        assertThat(result).isSameAs(expectedDto);

        verify(dataProductAccessGuard).verifyOwnedByCurrentProvider(DATA_PRODUCT_ID);
        verify(pdfValidator).validate("application/pdf", fileUpload.uploadedFile());

        var entityCaptor = ArgumentCaptor.forClass(DataProductDocumentEntity.class);
        verify(dataProductDocumentRepository).persist(entityCaptor.capture());
        var persisted = entityCaptor.getValue();
        assertThat(persisted.getId()).isNotNull();
        assertThat(persisted.getDataProduct()).isSameAs(dataProductEntity);
        assertThat(persisted.getOriginalFilename()).isEqualTo("document.pdf");
        assertThat(persisted.getSizeBytes()).isEqualTo(3L);
        assertThat(persisted.getScanStatus()).isEqualTo(DocumentScanStatusEnum.PENDING_SCAN);

        // S3 upload uses the same generated ID as the persisted entity.
        verify(dataProductDocumentStorageService).upload(persisted.getId(), FILE_CONTENT);
        verify(dataProductDocumentStorageService, never()).delete(any(UUID.class));

        // Scan polling is submitted asynchronously; the runnable itself needs an Arc container
        // and is deliberately not executed here.
        verify(managedExecutor).submit(any(Runnable.class));
      }
    }

    @Test
    void givenValidUpload_whenAddDocumentAsAdmin_thenVerifiesExistenceInsteadOfOwnership() throws Exception {
      stubValidUpload();
      when(fileUpload.fileName()).thenReturn("document.pdf");
      when(dataProductDocumentRepository.count("dataProduct.id", DATA_PRODUCT_ID)).thenReturn(0L);
      when(dataProductDocumentMapper.toDto(any(DataProductDocumentEntity.class)))
          .thenReturn(DataProductDocumentMetadataDto.builder().build());

      try (MockedStatic<QuarkusTransaction> quarkusTransaction = mockStatic(QuarkusTransaction.class)) {
        quarkusTransaction.when(QuarkusTransaction::requiringNew).thenReturn(transactionRunner);
        stubTransactionRunnerToInvokeCallable();

        service.addDataProductDocumentAsAdmin(DATA_PRODUCT_ID, fileUpload);

        verify(dataProductAccessGuard).verifyExists(DATA_PRODUCT_ID);
        verify(dataProductAccessGuard, never()).verifyOwnedByCurrentProvider(any());
      }
    }

    @Test
    void givenFileExceedsMaxSize_whenAddDocument_thenThrowsValidationExceptionBeforeAnySideEffects() {
      when(fileUpload.size()).thenReturn(10_485_761L);

      assertThatThrownBy(() -> service.addDataProductDocumentAsProvider(DATA_PRODUCT_ID, fileUpload))
          .isInstanceOf(ValidationException.class)
          .hasMessageContaining("maximum size");

      verifyNoInteractions(pdfValidator, dataProductDocumentStorageService,
          dataProductDocumentRepository, managedExecutor);
    }

    @Test
    void givenPdfValidationFails_whenAddDocument_thenPropagatesAndSkipsUpload() {
      when(fileUpload.size()).thenReturn(3L);
      when(fileUpload.contentType()).thenReturn("text/plain");
      doThrow(new ValidationException("not a pdf"))
          .when(pdfValidator).validate(any(), any());

      assertThatThrownBy(() -> service.addDataProductDocumentAsProvider(DATA_PRODUCT_ID, fileUpload))
          .isInstanceOf(ValidationException.class)
          .hasMessage("not a pdf");

      verifyNoInteractions(dataProductDocumentStorageService, dataProductDocumentRepository, managedExecutor);
    }

    @Test
    void givenDocumentLimitReached_whenAddDocument_thenThrowsValidationExceptionBeforeUpload() throws Exception {
      stubValidUpload();
      when(dataProductDocumentRepository.count("dataProduct.id", DATA_PRODUCT_ID)).thenReturn(5L);

      assertThatThrownBy(() -> service.addDataProductDocumentAsProvider(DATA_PRODUCT_ID, fileUpload))
          .isInstanceOf(ValidationException.class)
          .hasMessageContaining("at most");

      verifyNoInteractions(dataProductDocumentStorageService, managedExecutor);
    }

    @Test
    void givenLimitReachedByConcurrentUpload_whenAddDocument_thenSecondCheckUnderLockFailsAndOrphanIsDeleted() throws Exception {
      stubValidUpload();
      // Pre-check passes (0), authoritative check under the pessimistic lock fails (5):
      // simulates a concurrent upload winning the race between the two checks.
      when(dataProductDocumentRepository.count("dataProduct.id", DATA_PRODUCT_ID)).thenReturn(0L, 5L);
      when(dataProductRepository.findById(DATA_PRODUCT_ID, LockModeType.PESSIMISTIC_WRITE))
          .thenReturn(new DataProductEntity());

      try (MockedStatic<QuarkusTransaction> quarkusTransaction = mockStatic(QuarkusTransaction.class)) {
        quarkusTransaction.when(QuarkusTransaction::requiringNew).thenReturn(transactionRunner);
        stubTransactionRunnerToInvokeCallable();

        assertThatThrownBy(() -> service.addDataProductDocumentAsProvider(DATA_PRODUCT_ID, fileUpload))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("at most");

        var documentIdCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(dataProductDocumentStorageService).upload(documentIdCaptor.capture(), any(byte[].class));
        verify(dataProductDocumentStorageService).delete(documentIdCaptor.getValue());
        verify(dataProductDocumentRepository, never()).persist(any(DataProductDocumentEntity.class));
        verifyNoInteractions(managedExecutor);
      }
    }

    @Test
    void givenTransactionFails_whenAddDocument_thenDeletesOrphanedS3ObjectAndRethrows() throws Exception {
      stubValidUpload();
      when(dataProductDocumentRepository.count("dataProduct.id", DATA_PRODUCT_ID)).thenReturn(0L);

      try (MockedStatic<QuarkusTransaction> quarkusTransaction = mockStatic(QuarkusTransaction.class)) {
        quarkusTransaction.when(QuarkusTransaction::requiringNew).thenReturn(transactionRunner);
        when(transactionRunner.call(any())).thenThrow(new RuntimeException("transaction failed"));

        assertThatThrownBy(() -> service.addDataProductDocumentAsProvider(DATA_PRODUCT_ID, fileUpload))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("transaction failed");

        var documentIdCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(dataProductDocumentStorageService).upload(documentIdCaptor.capture(), any(byte[].class));
        verify(dataProductDocumentStorageService).delete(documentIdCaptor.getValue());

        verifyNoInteractions(managedExecutor, dataProductDocumentScanService);
      }
    }

    @Test
    void givenTransactionFailsAndOrphanCleanupFails_whenAddDocument_thenStillRethrowsOriginalException() throws Exception {
      stubValidUpload();
      when(dataProductDocumentRepository.count("dataProduct.id", DATA_PRODUCT_ID)).thenReturn(0L);

      try (MockedStatic<QuarkusTransaction> quarkusTransaction = mockStatic(QuarkusTransaction.class)) {
        quarkusTransaction.when(QuarkusTransaction::requiringNew).thenReturn(transactionRunner);
        when(transactionRunner.call(any())).thenThrow(new RuntimeException("transaction failed"));
        doThrow(new RuntimeException("s3 delete failed"))
            .when(dataProductDocumentStorageService).delete(any(UUID.class));

        assertThatThrownBy(() -> service.addDataProductDocumentAsProvider(DATA_PRODUCT_ID, fileUpload))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("transaction failed");

        verify(dataProductDocumentStorageService).delete(any(UUID.class));
      }
    }
  }

  @Nested
  class GetDocument {

    @Test
    void givenAvailableDocument_whenGetDocumentAsProvider_thenReturnsFilenameAndBytes() {
      var entity = documentEntity(DocumentScanStatusEnum.AVAILABLE);
      when(dataProductDocumentRepository.findByDataProductIdAndDocumentId(DATA_PRODUCT_ID, DOCUMENT_ID))
          .thenReturn(Optional.of(entity));
      when(dataProductDocumentStorageService.download(DOCUMENT_ID)).thenReturn(FILE_CONTENT);

      var result = service.getDataProductDocumentAsProvider(DATA_PRODUCT_ID, DOCUMENT_ID);

      verify(dataProductAccessGuard).verifyOwnedByCurrentProvider(DATA_PRODUCT_ID);
      assertThat(result.fileName()).isEqualTo("document.pdf");
      assertThat(result.content()).isEqualTo(FILE_CONTENT);
    }

    @Test
    void givenDocumentNotFound_whenGetDocument_thenThrowsNotFoundAndSkipsDownload() {
      when(dataProductDocumentRepository.findByDataProductIdAndDocumentId(DATA_PRODUCT_ID, DOCUMENT_ID))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.getDataProductDocumentAsProvider(DATA_PRODUCT_ID, DOCUMENT_ID))
          .isInstanceOf(NotFoundException.class);

      verifyNoInteractions(dataProductDocumentStorageService);
    }

    @Test
    void givenScanNotYetAvailable_whenGetDocument_thenThrowsForbiddenAndSkipsDownload() {
      var entity = documentEntity(DocumentScanStatusEnum.PENDING_SCAN);
      when(dataProductDocumentRepository.findByDataProductIdAndDocumentId(DATA_PRODUCT_ID, DOCUMENT_ID))
          .thenReturn(Optional.of(entity));

      assertThatThrownBy(() -> service.getDataProductDocumentAsProvider(DATA_PRODUCT_ID, DOCUMENT_ID))
          .isInstanceOf(ForbiddenException.class);

      verifyNoInteractions(dataProductDocumentStorageService);
    }

  }

  @Nested
  class DeleteDocument {

    @Test
    void givenExistingDocument_whenDeleteAsProvider_thenDeletesFromStorageAndRepository() {
      var entity = documentEntity(DocumentScanStatusEnum.AVAILABLE);
      when(dataProductDocumentRepository.findByDataProductIdAndDocumentId(DATA_PRODUCT_ID, DOCUMENT_ID))
          .thenReturn(Optional.of(entity));

      service.deleteDataProductDocumentAsProvider(DATA_PRODUCT_ID, DOCUMENT_ID);

      verify(dataProductAccessGuard).verifyOwnedByCurrentProvider(DATA_PRODUCT_ID);
      verify(dataProductDocumentStorageService).delete(DOCUMENT_ID);
      verify(dataProductDocumentRepository).delete(entity);
    }

    @Test
    void givenDocumentNotFound_whenDelete_thenThrowsNotFoundAndDeletesNothing() {
      when(dataProductDocumentRepository.findByDataProductIdAndDocumentId(DATA_PRODUCT_ID, DOCUMENT_ID))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.deleteDataProductDocumentAsProvider(DATA_PRODUCT_ID, DOCUMENT_ID))
          .isInstanceOf(NotFoundException.class);

      verifyNoInteractions(dataProductDocumentStorageService);
      verify(dataProductDocumentRepository, never()).delete(any(DataProductDocumentEntity.class));
    }
  }

  @Nested
  class GetDocumentsMetadata {

    @Test
    void givenDocumentsExist_whenGetMetadataListAsProvider_thenMapsAllEntities() {
      var entity1 = documentEntity(DocumentScanStatusEnum.AVAILABLE);
      var entity2 = documentEntity(DocumentScanStatusEnum.PENDING_SCAN);
      var dto1 = DataProductDocumentMetadataDto.builder().build();
      var dto2 = DataProductDocumentMetadataDto.builder().build();
      when(dataProductDocumentRepository.findByDataProductId(DATA_PRODUCT_ID))
          .thenReturn(List.of(entity1, entity2));
      when(dataProductDocumentMapper.toDto(entity1)).thenReturn(dto1);
      when(dataProductDocumentMapper.toDto(entity2)).thenReturn(dto2);

      var result = service.getDataProductDocumentsMetadataAsProvider(DATA_PRODUCT_ID);

      verify(dataProductAccessGuard).verifyOwnedByCurrentProvider(DATA_PRODUCT_ID);
      assertThat(result).containsExactly(dto1, dto2);
    }
  }

  @Nested
  class GetDocumentMetadata {

    @Test
    void givenLongPollingDisabled_whenGetMetadata_thenReturnsImmediatelyEvenIfPending() {
      var entity = documentEntity(DocumentScanStatusEnum.PENDING_SCAN);
      var expectedDto = DataProductDocumentMetadataDto.builder().build();
      when(dataProductDocumentRepository.findByDataProductIdAndDocumentId(DATA_PRODUCT_ID, DOCUMENT_ID))
          .thenReturn(Optional.of(entity));
      when(dataProductDocumentMapper.toDto(entity)).thenReturn(expectedDto);

      try (MockedStatic<QuarkusTransaction> quarkusTransaction = mockStatic(QuarkusTransaction.class)) {
        quarkusTransaction.when(QuarkusTransaction::requiringNew).thenReturn(transactionRunner);
        stubTransactionRunnerToInvokeCallable();

        long start = System.currentTimeMillis();
        var result = service.getDataProductDocumentMetadataAsProvider(DATA_PRODUCT_ID, DOCUMENT_ID, false);

        assertThat(result).isSameAs(expectedDto);
        // Guard against a regression that reintroduces polling when longPolling=false.
        assertThat(System.currentTimeMillis() - start).isLessThan(1_000);
        verify(dataProductDocumentRepository).findByDataProductIdAndDocumentId(DATA_PRODUCT_ID, DOCUMENT_ID);
      }
    }

    @Test
    void givenLongPollingEnabledAndScanAlreadyTerminal_whenGetMetadata_thenReturnsWithoutWaiting() {
      var entity = documentEntity(DocumentScanStatusEnum.AVAILABLE);
      var expectedDto = DataProductDocumentMetadataDto.builder().build();
      when(dataProductDocumentRepository.findByDataProductIdAndDocumentId(DATA_PRODUCT_ID, DOCUMENT_ID))
          .thenReturn(Optional.of(entity));
      when(dataProductDocumentMapper.toDto(entity)).thenReturn(expectedDto);

      try (MockedStatic<QuarkusTransaction> quarkusTransaction = mockStatic(QuarkusTransaction.class)) {
        quarkusTransaction.when(QuarkusTransaction::requiringNew).thenReturn(transactionRunner);
        stubTransactionRunnerToInvokeCallable();

        long start = System.currentTimeMillis();
        var result = service.getDataProductDocumentMetadataAsProvider(DATA_PRODUCT_ID, DOCUMENT_ID, true);

        assertThat(result).isSameAs(expectedDto);
        assertThat(System.currentTimeMillis() - start).isLessThan(1_000);
      }
    }

    @Test
    void givenLongPollingAndStatusChangesAfterFirstIteration_whenGetMetadata_thenReturnsUpdatedStatus() {
      var pendingEntity = documentEntity(DocumentScanStatusEnum.PENDING_SCAN);
      var availableEntity = documentEntity(DocumentScanStatusEnum.AVAILABLE);
      var expectedDto = DataProductDocumentMetadataDto.builder().build();
      // First read observes PENDING_SCAN, second (fresh transaction) observes AVAILABLE.
      when(dataProductDocumentRepository.findByDataProductIdAndDocumentId(DATA_PRODUCT_ID, DOCUMENT_ID))
          .thenReturn(Optional.of(pendingEntity), Optional.of(availableEntity));
      when(dataProductDocumentMapper.toDto(availableEntity)).thenReturn(expectedDto);

      try (MockedStatic<QuarkusTransaction> quarkusTransaction = mockStatic(QuarkusTransaction.class)) {
        quarkusTransaction.when(QuarkusTransaction::requiringNew).thenReturn(transactionRunner);
        stubTransactionRunnerToInvokeCallable();

        var result = service.getDataProductDocumentMetadataAsProvider(DATA_PRODUCT_ID, DOCUMENT_ID, true);

        assertThat(result).isSameAs(expectedDto);
        verify(dataProductDocumentMapper, never()).toDto(pendingEntity);
      }
    }

    @Test
    void givenDocumentNotFound_whenGetMetadata_thenThrowsNotFound() {
      when(dataProductDocumentRepository.findByDataProductIdAndDocumentId(DATA_PRODUCT_ID, DOCUMENT_ID))
          .thenReturn(Optional.empty());

      try (MockedStatic<QuarkusTransaction> quarkusTransaction = mockStatic(QuarkusTransaction.class)) {
        quarkusTransaction.when(QuarkusTransaction::requiringNew).thenReturn(transactionRunner);
        stubTransactionRunnerToInvokeCallable();

        assertThatThrownBy(() ->
            service.getDataProductDocumentMetadataAsProvider(DATA_PRODUCT_ID, DOCUMENT_ID, true))
            .isInstanceOf(NotFoundException.class);
      }
    }
  }
}