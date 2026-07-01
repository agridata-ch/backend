package ch.agridata.aws.service;

import ch.agridata.aws.api.GuardDutyScanResultEnum;
import ch.agridata.aws.api.PdfStorageApi;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Stores, retrieves and deletes PDF files in an S3 bucket and reads the virus scan results.
 *
 * @CommentLastReviewed 2026-07-10
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class PdfS3Service implements PdfStorageApi {


  private final S3Client s3Client;

  @Override
  public void upload(String bucket, String fileName, byte[] pdf) {
    String key = fileName + ".pdf";
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

  @Override
  public GuardDutyScanResultEnum readScanResult(String bucket, String fileName) {
    String key = fileName + ".pdf";
    try {
      GetObjectTaggingResponse response = s3Client.getObjectTagging(
          GetObjectTaggingRequest.builder()
              .bucket(bucket)
              .key(key)
              .build());
      return response.tagSet().stream()
          .filter(tag -> GUARDDUTY_TAG_KEY.equals(tag.key()))
          .findFirst()
          .map(tag -> parseScanResult(tag.value()))
          .orElse(null);
    } catch (S3Exception e) {
      log.error("Failed to read scan result tag from S3: bucket={}, key={}: {}",
          bucket, key, e.awsErrorDetails().errorMessage());
      throw new ExternalWebServiceException("Failed to read scan result from S3.", e);
    }
  }

  private static GuardDutyScanResultEnum parseScanResult(String value) {
    try {
      return GuardDutyScanResultEnum.valueOf(value);
    } catch (IllegalArgumentException _) {
      log.warn("Unrecognized GuardDuty scan status tag value '{}'; treating as UNKNOWN", value);
      return GuardDutyScanResultEnum.UNKNOWN;
    }
  }

  @Override
  public byte[] download(String bucket, String fileName) {
    String key = fileName + ".pdf";
    try {
      byte[] pdf = s3Client.getObjectAsBytes(
              GetObjectRequest.builder()
                  .bucket(bucket)
                  .key(key)
                  .build())
          .asByteArray();
      log.info("Downloaded PDF from S3: bucket={}, key={}", bucket, key);
      return pdf;
    } catch (S3Exception e) {
      log.error("Failed to download PDF from S3: bucket={}, key={}: {}", bucket, key, e.awsErrorDetails().errorMessage());
      throw new ExternalWebServiceException("Failed to download PDF from S3.", e);
    }
  }

  @Override
  public void delete(String bucket, String fileName) {
    String key = fileName + ".pdf";
    try {
      s3Client.deleteObject(DeleteObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .build());
      log.info("Deleted PDF from S3: bucket={}, key={}", bucket, key);
    } catch (S3Exception e) {
      log.error("Failed to delete PDF from S3: bucket={}, key={}: {}",
          bucket, key, e.awsErrorDetails().errorMessage());
      throw new ExternalWebServiceException("Failed to delete PDF from S3.", e);
    }
  }
}
