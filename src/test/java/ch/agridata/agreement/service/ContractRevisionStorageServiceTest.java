package ch.agridata.agreement.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.aws.api.PdfStorageApi;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractRevisionStorageServiceTest {

  @Mock
  private PdfStorageApi pdfStorageApi;

  @InjectMocks
  private ContractRevisionStorageService service;

  private static final UUID REVISION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final String BUCKET = "test-bucket";
  private static final String EXPECTED_FILE_NAME = "contract-revision_" + REVISION_ID;
  private static final byte[] PDF_BYTES = {1, 2, 3};

  @BeforeEach
  void setUp() {
    service.contractBucketName = BUCKET;
  }

  @Test
  void upload_delegatesToApiWithCorrectFileName() {
    service.upload(REVISION_ID, PDF_BYTES);

    verify(pdfStorageApi).upload(BUCKET, EXPECTED_FILE_NAME, PDF_BYTES);
  }

  @Test
  void download_delegatesToApiWithCorrectFileName() {
    when(pdfStorageApi.download(BUCKET, EXPECTED_FILE_NAME)).thenReturn(PDF_BYTES);

    byte[] result = service.download(REVISION_ID);

    verify(pdfStorageApi).download(BUCKET, EXPECTED_FILE_NAME);
    assertThat(result).isEqualTo(PDF_BYTES);
  }
}
