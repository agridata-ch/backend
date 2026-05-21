package ch.agridata.aws.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

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
}
