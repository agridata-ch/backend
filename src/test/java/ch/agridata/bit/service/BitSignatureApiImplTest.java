package ch.agridata.bit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.bit.dto.BitAddHashRequest;
import ch.agridata.bit.dto.BitAddHashResponse;
import ch.agridata.bit.dto.BitCheckSignStateRequest;
import ch.agridata.bit.dto.BitCheckSignStateResponse;
import ch.agridata.bit.dto.BitGetSignedHashesResponse;
import ch.agridata.bit.dto.BitInitSignRequest;
import ch.agridata.bit.dto.BitInitSignResponse;
import ch.agridata.bit.dto.BitSignReturnStatusCode;
import ch.agridata.bit.dto.BitSignState;
import ch.agridata.bit.dto.BitSignatureEntry;
import ch.agridata.bit.dto.BitStartSignResponse;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BitSignatureApiImplTest {

  private static final String TOKEN = "test-sign-process-token";
  private static final String ADMIN_GLOBAL_ID = "admin-123";
  private static final String KEY_BEARER = "test-key-bearer";
  private static final String PROFILE = "test-profile";
  // PDFBox accepts any bytes as the embedded signature; 4 bytes are enough for unit tests
  private static final String DUMMY_SIGNATURE_BASE64 = Base64.getEncoder().encodeToString(new byte[] {1, 2, 3, 4});

  @Mock
  BitSignatureServiceRestClient restClient;

  @InjectMocks
  BitSignatureApiImpl bitSignatureApiImpl;

  @BeforeEach
  void setUp() throws Exception {
    setField(bitSignatureApiImpl, "keyBearer", KEY_BEARER);
    setField(bitSignatureApiImpl, "profile", PROFILE);
  }

  @Test
  void givenValidPdf_whenSign_thenCallsAllBitEndpointsAndReturnsSignedPdfBytes() {
    // given
    mockHappyPath();

    // when
    byte[] result = bitSignatureApiImpl.sign(minimalPdf(), ADMIN_GLOBAL_ID);

    // then
    assertThat(result).isNotEmpty();

    var initCaptor = ArgumentCaptor.forClass(BitInitSignRequest.class);
    verify(restClient).initSign(initCaptor.capture());
    assertThat(initCaptor.getValue().adminGlobalId()).isEqualTo(ADMIN_GLOBAL_ID);
    assertThat(initCaptor.getValue().keyBearer()).isEqualTo(KEY_BEARER);
    assertThat(initCaptor.getValue().profile()).isEqualTo(PROFILE);
    assertThat(initCaptor.getValue().authType()).isEqualTo("mobileid");

    var addHashCaptor = ArgumentCaptor.forClass(BitAddHashRequest.class);
    verify(restClient).addHash(addHashCaptor.capture());
    assertThat(addHashCaptor.getValue().signProcessToken()).isEqualTo(TOKEN);
    assertThat(addHashCaptor.getValue().signatureAlgorithm()).isEqualTo("SHA256");
    assertThat(addHashCaptor.getValue().pkcsVersion()).isEqualTo("PKCS7");
    assertThat(addHashCaptor.getValue().digest()).isNotBlank();
    assertThat(addHashCaptor.getValue().tag()).isNotBlank();

    var checkCaptor = ArgumentCaptor.forClass(BitCheckSignStateRequest.class);
    verify(restClient).checkSignState(checkCaptor.capture());
    assertThat(checkCaptor.getValue().signProcessToken()).isEqualTo(TOKEN);
    assertThat(checkCaptor.getValue().longPolling()).isTrue();

    verify(restClient).startSign(any());
    verify(restClient).getSignedHashes(any());
    verify(restClient).dropSign(any());
  }

  @Test
  void givenInitSignNonOkStatus_whenSign_thenThrowsWithoutCallingDropSign() {
    // given
    when(restClient.initSign(any()))
        .thenReturn(new BitInitSignResponse(BitSignReturnStatusCode.ERROR, "log-1", null));

    // when / then
    byte[] pdf = minimalPdf();
    assertThatThrownBy(() -> bitSignatureApiImpl.sign(pdf, ADMIN_GLOBAL_ID))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("initSign failed");

    verify(restClient, never()).dropSign(any());
  }

  @Test
  void givenAddHashNonOkStatus_whenSign_thenThrowsAndCallsDropSign() {
    // given
    when(restClient.initSign(any())).thenReturn(initOk());
    when(restClient.addHash(any()))
        .thenReturn(new BitAddHashResponse(BitSignReturnStatusCode.ERROR, "log-2"));

    // when / then
    byte[] pdf = minimalPdf();
    assertThatThrownBy(() -> bitSignatureApiImpl.sign(pdf, ADMIN_GLOBAL_ID))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("addHash failed");

    verify(restClient).dropSign(any());
  }

  @Test
  void givenStartSignNonOkStatus_whenSign_thenThrowsAndCallsDropSign() {
    // given
    when(restClient.initSign(any())).thenReturn(initOk());
    when(restClient.addHash(any())).thenReturn(addHashOk());
    when(restClient.startSign(any()))
        .thenReturn(new BitStartSignResponse(BitSignReturnStatusCode.ERROR, "log-3", null, null));

    // when / then
    byte[] pdf = minimalPdf();
    assertThatThrownBy(() -> bitSignatureApiImpl.sign(pdf, ADMIN_GLOBAL_ID))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("startSign failed");

    verify(restClient).dropSign(any());
  }

  @Test
  void givenSignStateCanceled_whenSign_thenThrowsAndCallsDropSign() {
    // given
    when(restClient.initSign(any())).thenReturn(initOk());
    when(restClient.addHash(any())).thenReturn(addHashOk());
    when(restClient.startSign(any())).thenReturn(startSignOk());
    when(restClient.checkSignState(any())).thenReturn(checkSignState(BitSignState.SIGN_CANCELED));

    // when / then
    byte[] pdf = minimalPdf();
    assertThatThrownBy(() -> bitSignatureApiImpl.sign(pdf, ADMIN_GLOBAL_ID))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("cancelled");

    verify(restClient).dropSign(any());
  }

  @Test
  void givenSignStateInvalidState_whenSign_thenThrowsAndCallsDropSign() {
    // given
    when(restClient.initSign(any())).thenReturn(initOk());
    when(restClient.addHash(any())).thenReturn(addHashOk());
    when(restClient.startSign(any())).thenReturn(startSignOk());
    when(restClient.checkSignState(any())).thenReturn(checkSignState(BitSignState.SIGN_INVALID_STATE));

    // when / then
    byte[] pdf = minimalPdf();
    assertThatThrownBy(() -> bitSignatureApiImpl.sign(pdf, ADMIN_GLOBAL_ID))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("unexpected state");

    verify(restClient).dropSign(any());
  }

  @Test
  void givenSignStateAlwaysRunning_whenSign_thenThrowsAfterMaxIterationsAndCallsDropSign() {
    // given
    when(restClient.initSign(any())).thenReturn(initOk());
    when(restClient.addHash(any())).thenReturn(addHashOk());
    when(restClient.startSign(any())).thenReturn(startSignOk());
    when(restClient.checkSignState(any())).thenReturn(checkSignState(BitSignState.SIGN_RUNNING));

    // when / then
    byte[] pdf = minimalPdf();
    assertThatThrownBy(() -> bitSignatureApiImpl.sign(pdf, ADMIN_GLOBAL_ID))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("maximum polling iterations");

    verify(restClient, times(10)).checkSignState(any()); // MAX_POLL_ITERATIONS = 10
    verify(restClient).dropSign(any());
  }

  @Test
  void givenSignStateRunningThenFinished_whenSign_thenPollsUntilFinishedAndSucceeds() {
    // given
    when(restClient.initSign(any())).thenReturn(initOk());
    when(restClient.addHash(any())).thenReturn(addHashOk());
    when(restClient.startSign(any())).thenReturn(startSignOk());
    when(restClient.checkSignState(any()))
        .thenReturn(checkSignState(BitSignState.SIGN_RUNNING))
        .thenReturn(checkSignState(BitSignState.SIGN_RUNNING))
        .thenReturn(checkSignState(BitSignState.SIGN_FINISHED));
    when(restClient.getSignedHashes(any())).thenReturn(getSignedHashesOk());

    // when
    byte[] result = bitSignatureApiImpl.sign(minimalPdf(), ADMIN_GLOBAL_ID);

    // then
    assertThat(result).isNotEmpty();
    verify(restClient, times(3)).checkSignState(any());
  }

  @Test
  void givenGetSignedHashesNonOkStatus_whenSign_thenThrowsAndCallsDropSign() {
    // given
    when(restClient.initSign(any())).thenReturn(initOk());
    when(restClient.addHash(any())).thenReturn(addHashOk());
    when(restClient.startSign(any())).thenReturn(startSignOk());
    when(restClient.checkSignState(any())).thenReturn(checkSignState(BitSignState.SIGN_FINISHED));
    when(restClient.getSignedHashes(any()))
        .thenReturn(new BitGetSignedHashesResponse(BitSignReturnStatusCode.ERROR, "log-5", null, null, null, null));

    // when / then
    byte[] pdf = minimalPdf();
    assertThatThrownBy(() -> bitSignatureApiImpl.sign(pdf, ADMIN_GLOBAL_ID))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("getSignedHashes failed");

    verify(restClient).dropSign(any());
  }

  @Test
  void givenGetSignedHashesEmptySignatures_whenSign_thenThrowsAndCallsDropSign() {
    // given
    when(restClient.initSign(any())).thenReturn(initOk());
    when(restClient.addHash(any())).thenReturn(addHashOk());
    when(restClient.startSign(any())).thenReturn(startSignOk());
    when(restClient.checkSignState(any())).thenReturn(checkSignState(BitSignState.SIGN_FINISHED));
    when(restClient.getSignedHashes(any()))
        .thenReturn(new BitGetSignedHashesResponse(BitSignReturnStatusCode.OK, null, null, null, null, List.of()));

    // when / then
    byte[] pdf = minimalPdf();
    assertThatThrownBy(() -> bitSignatureApiImpl.sign(pdf, ADMIN_GLOBAL_ID))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("no signatures");

    verify(restClient).dropSign(any());
  }

  @Test
  void givenDropSignThrows_whenSign_thenExceptionIsSwallowedAndSignedPdfIsReturned() {
    // given
    mockHappyPath();
    doThrow(new RuntimeException("network error")).when(restClient).dropSign(any());

    // when / then – dropSign exception must not propagate
    assertThat(bitSignatureApiImpl.sign(minimalPdf(), ADMIN_GLOBAL_ID)).isNotEmpty();
  }

  private void mockHappyPath() {
    when(restClient.initSign(any())).thenReturn(initOk());
    when(restClient.addHash(any())).thenReturn(addHashOk());
    when(restClient.startSign(any())).thenReturn(startSignOk());
    when(restClient.checkSignState(any())).thenReturn(checkSignState(BitSignState.SIGN_FINISHED));
    when(restClient.getSignedHashes(any())).thenReturn(getSignedHashesOk());
  }

  private static BitInitSignResponse initOk() {
    return new BitInitSignResponse(BitSignReturnStatusCode.OK, null, TOKEN);
  }

  private static BitAddHashResponse addHashOk() {
    return new BitAddHashResponse(BitSignReturnStatusCode.OK, null);
  }

  private static BitStartSignResponse startSignOk() {
    return new BitStartSignResponse(BitSignReturnStatusCode.OK, null, null, null);
  }

  private static BitCheckSignStateResponse checkSignState(BitSignState state) {
    return new BitCheckSignStateResponse(BitSignReturnStatusCode.OK, null, state);
  }

  private static BitGetSignedHashesResponse getSignedHashesOk() {
    var entry = new BitSignatureEntry(DUMMY_SIGNATURE_BASE64, "PKCS7", "tag", null, null, null);
    return new BitGetSignedHashesResponse(BitSignReturnStatusCode.OK, null, null, null, null, List.of(entry));
  }

  private static byte[] minimalPdf() {
    try (var doc = new PDDocument(); var out = new ByteArrayOutputStream()) {
      doc.addPage(new PDPage());
      doc.save(out);
      return out.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException("Failed to create minimal test PDF", e);
    }
  }

  private static void setField(Object target, String name, Object value) throws Exception {
    var field = target.getClass().getDeclaredField(name);
    field.setAccessible(true);
    field.set(target, value);
  }
}
