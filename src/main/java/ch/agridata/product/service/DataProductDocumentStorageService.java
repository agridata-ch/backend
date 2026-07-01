package ch.agridata.product.service;

import ch.agridata.aws.api.GuardDutyScanResultEnum;
import ch.agridata.aws.api.PdfStorageApi;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * This class provides services for storing and retrieving data product pdf-documents.
 *
 * @CommentLastReviewed 2026-07-09
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataProductDocumentStorageService {
  @ConfigProperty(name = "agridata.product.uploads-bucket-name")
  String uploadsBucketName;

  private final PdfStorageApi pdfStorageApi;

  public void upload(@NonNull UUID dataProductDocumentId, byte[] pdf) {
    pdfStorageApi.upload(uploadsBucketName, buildFileName(dataProductDocumentId), pdf);
  }

  public GuardDutyScanResultEnum readScanResult(@NonNull UUID dataProductDocumentId) {
    return pdfStorageApi.readScanResult(uploadsBucketName, buildFileName(dataProductDocumentId));
  }

  public byte[] download(@NonNull UUID dataProductDocumentId) {
    return pdfStorageApi.download(uploadsBucketName, buildFileName(dataProductDocumentId));
  }

  public void delete(@NonNull UUID dataProductDocumentId) {
    pdfStorageApi.delete(uploadsBucketName, buildFileName(dataProductDocumentId));
  }

  private static String buildFileName(@NonNull UUID documentId) {
    return String.format("data-product/%s", documentId);
  }
}
