package ch.agridata.aws.service;

import ch.agridata.aws.api.PdfStorageApi;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Stores PDF files in an S3 bucket.
 *
 * @CommentLastReviewed 2026-04-17
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class PdfS3Service implements PdfStorageApi {

  private final S3Client s3Client;

  @Override
  public void upload(String bucket, UUID id, byte[] pdf) {
    String key = id + ".pdf";
    try {
      s3Client.putObject(
          PutObjectRequest.builder()
              .bucket(bucket)
              .key(key)
              .contentType("application/pdf")
              .build(),
          RequestBody.fromBytes(pdf));
      log.info("Stored PDF in S3: bucket={}, key={}", bucket, key);
    } catch (S3Exception e) {
      log.error("Failed to upload PDF to S3: bucket={}, key={}: {}", bucket, key, e.awsErrorDetails().errorMessage());
      throw new ExternalWebServiceException("Failed to upload PDF to S3.", e);
    }
  }
}
