package ch.agridata.aws.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.aws.api.GuardDutyScanResultEnum;
import ch.agridata.aws.api.PdfStorageApi;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.Tag;

@ExtendWith(MockitoExtension.class)
class PdfS3ServiceTest {

  @Mock
  private S3Client s3Client;

  @InjectMocks
  private PdfS3Service pdfS3Service;

  @Captor
  private ArgumentCaptor<PutObjectRequest> putRequestCaptor;

  @Captor
  private ArgumentCaptor<GetObjectRequest> getRequestCaptor;

  @Captor
  private ArgumentCaptor<GetObjectTaggingRequest> taggingRequestCaptor;

  @Captor
  private ArgumentCaptor<DeleteObjectRequest> deleteRequestCaptor;

  private static final String BUCKET = "test-bucket";
  private static final String FILE_NAME = "contract-revision_abc123";
  private static final byte[] PDF_BYTES = {37, 80, 68, 70}; // %PDF

  @Test
  void upload_success() {
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenReturn(PutObjectResponse.builder().build());

    pdfS3Service.upload(BUCKET, FILE_NAME, PDF_BYTES);

    verify(s3Client).putObject(putRequestCaptor.capture(), any(RequestBody.class));
    PutObjectRequest request = putRequestCaptor.getValue();
    assertThat(request.bucket()).isEqualTo(BUCKET);
    assertThat(request.key()).isEqualTo(FILE_NAME + ".pdf");
    assertThat(request.contentType()).isEqualTo("application/pdf");
  }

  @Test
  void upload_s3Exception_throwsExternalWebServiceException() {
    S3Exception s3Exception = (S3Exception) S3Exception.builder()
        .awsErrorDetails(AwsErrorDetails.builder().errorMessage("Access denied").build())
        .build();
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenThrow(s3Exception);

    assertThatThrownBy(() -> pdfS3Service.upload(BUCKET, FILE_NAME, PDF_BYTES))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("Failed to upload PDF to S3");
  }

  @Test
  void download_success() {
    ResponseBytes<GetObjectResponse> responseBytes =
        ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), PDF_BYTES);
    when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);

    byte[] result = pdfS3Service.download(BUCKET, FILE_NAME);

    verify(s3Client).getObjectAsBytes(getRequestCaptor.capture());
    GetObjectRequest request = getRequestCaptor.getValue();
    assertThat(request.bucket()).isEqualTo(BUCKET);
    assertThat(request.key()).isEqualTo(FILE_NAME + ".pdf");
    assertThat(result).isEqualTo(PDF_BYTES);
  }

  @Test
  void download_s3Exception_throwsExternalWebServiceException() {
    S3Exception s3Exception = (S3Exception) S3Exception.builder()
        .awsErrorDetails(AwsErrorDetails.builder().errorMessage("NoSuchKey").build())
        .build();
    when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenThrow(s3Exception);

    assertThatThrownBy(() -> pdfS3Service.download(BUCKET, FILE_NAME))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("Failed to download PDF from S3");
  }

  @Test
  void readScanResult_success() {
    GetObjectTaggingResponse response = GetObjectTaggingResponse.builder()
        .tagSet(Tag.builder().key(PdfStorageApi.GUARDDUTY_TAG_KEY).value("NO_THREATS_FOUND").build())
        .build();
    when(s3Client.getObjectTagging(any(GetObjectTaggingRequest.class))).thenReturn(response);

    GuardDutyScanResultEnum result = pdfS3Service.readScanResult(BUCKET, FILE_NAME);

    verify(s3Client).getObjectTagging(taggingRequestCaptor.capture());
    GetObjectTaggingRequest request = taggingRequestCaptor.getValue();
    assertThat(request.bucket()).isEqualTo(BUCKET);
    assertThat(request.key()).isEqualTo(FILE_NAME + ".pdf");
    assertThat(result).isEqualTo(GuardDutyScanResultEnum.NO_THREATS_FOUND);
  }

  @Test
  void readScanResult_unrecognizedTagValue_returnsUnknown() {
    GetObjectTaggingResponse response = GetObjectTaggingResponse.builder()
        .tagSet(Tag.builder().key(PdfStorageApi.GUARDDUTY_TAG_KEY).value("SOME_NEW_STATUS").build())
        .build();
    when(s3Client.getObjectTagging(any(GetObjectTaggingRequest.class))).thenReturn(response);

    assertThat(pdfS3Service.readScanResult(BUCKET, FILE_NAME)).isEqualTo(GuardDutyScanResultEnum.UNKNOWN);
  }

  @Test
  void readScanResult_noMatchingTag_returnsNull() {
    GetObjectTaggingResponse response = GetObjectTaggingResponse.builder()
        .tagSet(Tag.builder().key("SomeOtherTag").value("whatever").build())
        .build();
    when(s3Client.getObjectTagging(any(GetObjectTaggingRequest.class))).thenReturn(response);

    assertThat(pdfS3Service.readScanResult(BUCKET, FILE_NAME)).isNull();
  }

  @Test
  void readScanResult_s3Exception_throwsExternalWebServiceException() {
    S3Exception s3Exception = (S3Exception) S3Exception.builder()
        .awsErrorDetails(AwsErrorDetails.builder().errorMessage("Access denied").build())
        .build();
    when(s3Client.getObjectTagging(any(GetObjectTaggingRequest.class))).thenThrow(s3Exception);

    assertThatThrownBy(() -> pdfS3Service.readScanResult(BUCKET, FILE_NAME))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("Failed to read scan result from S3");
  }

  @Test
  void delete_success() {
    when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
        .thenReturn(DeleteObjectResponse.builder().build());

    pdfS3Service.delete(BUCKET, FILE_NAME);

    verify(s3Client).deleteObject(deleteRequestCaptor.capture());
    DeleteObjectRequest request = deleteRequestCaptor.getValue();
    assertThat(request.bucket()).isEqualTo(BUCKET);
    assertThat(request.key()).isEqualTo(FILE_NAME + ".pdf");
  }

  @Test
  void delete_s3Exception_throwsExternalWebServiceException() {
    S3Exception s3Exception = (S3Exception) S3Exception.builder()
        .awsErrorDetails(AwsErrorDetails.builder().errorMessage("Access denied").build())
        .build();
    when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(s3Exception);

    assertThatThrownBy(() -> pdfS3Service.delete(BUCKET, FILE_NAME))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("Failed to delete PDF from S3");
  }
}
