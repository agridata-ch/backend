package ch.agridata.agreement.service;

import ch.agridata.aws.api.PdfStorageApi;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Handles storage and retrieval of contract revision PDFs in object storage.
 *
 * @CommentLastReviewed 2026-04-23
 */
@ApplicationScoped
@RequiredArgsConstructor
public class ContractRevisionStorageService {

  @ConfigProperty(name = "agridata.agreement.contract-bucket-name")
  String contractBucketName;

  private final PdfStorageApi pdfStorageApi;

  public void upload(@NonNull UUID contractRevisionId, byte[] pdf) {
    pdfStorageApi.upload(contractBucketName, buildFileName(contractRevisionId), pdf);
  }

  public byte[] download(@NonNull UUID contractRevisionId) {
    return pdfStorageApi.download(contractBucketName, buildFileName(contractRevisionId));
  }

  private static String buildFileName(@NonNull UUID contractRevisionId) {
    return String.format("contract-revision_%s", contractRevisionId);
  }
}
