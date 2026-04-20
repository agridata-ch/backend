package ch.agridata.agreement.service;

import ch.agridata.aws.api.PdfStorageApi;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Handles storage of contract revision PDFs in object storage.
 *
 * @CommentLastReviewed 2026-04-20
 */
@ApplicationScoped
@RequiredArgsConstructor
public class ContractRevisionStorageService {

  @ConfigProperty(name = "agridata.agreement.contract-bucket-name")
  String contractBucketName;

  private final PdfStorageApi pdfStorageApi;

  public void upload(UUID contractRevisionId, byte[] pdf) {
    pdfStorageApi.upload(contractBucketName, contractRevisionId, pdf);
  }
}
