package ch.agridata.bit.service;

import ch.agridata.bit.api.BitSignatureApi;
import ch.agridata.bit.dto.BitAddHashRequest;
import ch.agridata.bit.dto.BitCheckSignStateRequest;
import ch.agridata.bit.dto.BitCheckSignStateResponse;
import ch.agridata.bit.dto.BitDropSignRequest;
import ch.agridata.bit.dto.BitGetSignedHashesRequest;
import ch.agridata.bit.dto.BitInitSignRequest;
import ch.agridata.bit.dto.BitInitSignResponse;
import ch.agridata.bit.dto.BitSignReturnStatusCode;
import ch.agridata.bit.dto.BitSignState;
import ch.agridata.bit.dto.BitStartSignRequest;
import ch.agridata.bit.dto.BitStartSignResponse;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jspecify.annotations.NonNull;

/**
 * Seals a PDF document using the BIT evidence Signing API with Mobile ID as the authentication
 * method for the declaration of intent.
 *
 * <p>The flow executed per call to {@link #sign}:
 * <ol>
 *   <li>Validate and prepare the PDF: insert a PKCS#7 signature placeholder via PDFBox</li>
 *   <li>Compute the SHA-256 hash over the byte ranges defined by the placeholder</li>
 *   <li>{@code initSign} – create a sign process with Mobile ID auth on BIT side</li>
 *   <li>{@code addHash} – submit the Base64-encoded hash</li>
 *   <li>{@code startSign} – trigger a Mobile ID push notification to the user</li>
 *   <li>{@code checkSignState} – long-poll until {@code SIGN_FINISHED} or a terminal error state</li>
 *   <li>{@code getSignedHashes} – retrieve the PKCS#7 CMS signature</li>
 *   <li>Embed the CMS signature into the PDF placeholder</li>
 *   <li>{@code dropSign} – clean up the sign process on BIT side (always, even on error)</li>
 * </ol>
 *
 * <p>Note on {@code interactionUrl}: with Mobile ID the BIT service sends a push notification
 * directly to the user's device. The returned {@code interactionUrl} (if any) is logged but not
 * forwarded to the client, as no browser interaction is required.
 *
 * @CommentLastReviewed 2026-04-09
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class BitSignatureApiImpl implements BitSignatureApi {

  private static final String SIGNATURE_NAME = "agridata.ch";
  private static final String SIGNATURE_REASON = "Sealed by agridata.ch";
  private static final String SIGNATURE_CONTACT = "support@agridata.ch";

  private static final String DIGEST_ALGORITHM = "SHA-256";
  private static final String BIT_SIGNATURE_ALGORITHM = "SHA256";
  private static final String PKCS_VERSION = "PKCS7";
  private static final int PREFERRED_SIGNATURE_SIZE = 0x10000; // 64 KB
  private static final int MAX_POLL_ITERATIONS = 10;
  private static final String AUTH_TYPE_MOBILE_ID = "mobileid";

  @RestClient
  BitSignatureServiceRestClient restClient;

  @ConfigProperty(name = "bit.signature.key-bearer")
  String keyBearer;

  @ConfigProperty(name = "bit.signature.profile")
  String profile;

  @Override
  public byte[] sign(byte @NonNull [] documentBytes, @NonNull String adminGlobalId) {
    try (var document = Loader.loadPDF(documentBytes);
         var output = new ByteArrayOutputStream();
         var signatureOptions = new SignatureOptions()) {

      signatureOptions.setPreferredSignatureSize(PREFERRED_SIGNATURE_SIZE);
      document.addSignature(createSignaturePlaceholder(), signatureOptions);

      var externalSigning = document.saveIncrementalForExternalSigning(output);
      var hashBase64 = computeHashBase64(externalSigning.getContent());

      byte[] signatureBytes = signHash(hashBase64, adminGlobalId);

      externalSigning.setSignature(signatureBytes);
      return output.toByteArray();
    } catch (IOException e) {
      log.error("PDF processing error during signing", e);
      throw new IllegalStateException("PDF processing failed", e);
    }
  }

  private byte[] signHash(String hashBase64, String adminGlobalId) {
    var initResponse = initSign(adminGlobalId);
    String signProcessToken = initResponse.signProcessToken();
    String tag = UUID.randomUUID().toString();
    log.debug("BIT sign process correlation tag: token={}, tag={}", signProcessToken, tag);

    try {
      addHash(signProcessToken, hashBase64, tag);
      startSign(signProcessToken);
      pollUntilFinished(signProcessToken);
      return retrieveSignature(signProcessToken);
    } finally {
      dropSign(signProcessToken);
    }
  }

  private PDSignature createSignaturePlaceholder() {
    PDSignature signature = new PDSignature();
    signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
    signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
    signature.setName(SIGNATURE_NAME);
    signature.setReason(SIGNATURE_REASON);
    signature.setContactInfo(SIGNATURE_CONTACT);
    signature.setSignDate(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
    return signature;
  }

  private String computeHashBase64(InputStream content) throws IOException {
    try {
      MessageDigest digest = MessageDigest.getInstance(DIGEST_ALGORITHM);
      try (content; var dos = new DigestOutputStream(OutputStream.nullOutputStream(), digest)) {
        content.transferTo(dos);
      }
      return Base64.getEncoder().encodeToString(digest.digest());
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm not available on this JVM", e);
    }
  }

  private BitInitSignResponse initSign(String adminGlobalId) {
    var request = new BitInitSignRequest(keyBearer, profile, "DE", null, AUTH_TYPE_MOBILE_ID, adminGlobalId);
    var response = restClient.initSign(request);
    if (response.status() != BitSignReturnStatusCode.OK) {
      throw new ExternalWebServiceException("BIT initSign failed: status=" + response.status() + ", logId=" + response.logId());
    }
    log.debug("BIT sign process created: token={}", response.signProcessToken());
    return response;
  }

  private void addHash(String signProcessToken, String hashBase64, String tag) {
    var request = new BitAddHashRequest(signProcessToken, hashBase64, BIT_SIGNATURE_ALGORITHM, PKCS_VERSION, tag);
    var response = restClient.addHash(request);
    if (response.status() != BitSignReturnStatusCode.OK) {
      throw new ExternalWebServiceException(
          "BIT addHash failed: status=" + response.status() + ", logId=" + response.logId());
    }
  }

  private void startSign(String signProcessToken) {
    BitStartSignResponse response = restClient.startSign(new BitStartSignRequest(signProcessToken));
    if (response.status() != BitSignReturnStatusCode.OK) {
      throw new ExternalWebServiceException(
          "BIT startSign failed: status=" + response.status() + ", logId=" + response.logId());
    }
    if (response.interactionUrl() != null) {
      log.info("BIT signing process started in INTERACTIVE mode. Mobile ID push notification sent. interactionUrl={}",
          response.interactionUrl());
    } else {
      log.debug("BIT signing process started in DIRECT mode.");
    }
  }

  private void pollUntilFinished(String signProcessToken) {
    for (int i = 0; i < MAX_POLL_ITERATIONS; i++) {
      BitCheckSignStateResponse response = restClient.checkSignState(new BitCheckSignStateRequest(signProcessToken, true));

      if (response.status() != BitSignReturnStatusCode.OK) {
        throw new ExternalWebServiceException(
            "BIT checkSignState failed: status=" + response.status() + ", logId=" + response.logId());
      }

      BitSignState state = response.signState();
      log.debug("BIT sign state poll {}/{}: state={}", i + 1, MAX_POLL_ITERATIONS, state);

      switch (state) {
        case SIGN_FINISHED -> {
          return;
        }
        case SIGN_RUNNING -> { /* continue polling */
        }
        case SIGN_CANCELED -> throw new ExternalWebServiceException("BIT signing process was cancelled by the user.");
        default -> throw new ExternalWebServiceException("BIT signing process ended in unexpected state: " + state);
      }
    }
    throw new ExternalWebServiceException(
        "BIT signing process did not finish within the maximum polling iterations (" + MAX_POLL_ITERATIONS + ").");
  }

  private byte[] retrieveSignature(String signProcessToken) {
    var response = restClient.getSignedHashes(new BitGetSignedHashesRequest(signProcessToken));
    if (response.status() != BitSignReturnStatusCode.OK) {
      throw new ExternalWebServiceException(
          "BIT getSignedHashes failed: status=" + response.status() + ", logId=" + response.logId());
    }
    if (response.signatures() == null || response.signatures().isEmpty()) {
      throw new ExternalWebServiceException("BIT getSignedHashes: response contains no signatures.");
    }
    var signatureBytes = Base64.getDecoder().decode(response.signatures().getFirst().signature());
    log.debug("BIT CMS size: {} bytes", signatureBytes.length);
    return signatureBytes;
  }

  private void dropSign(String signProcessToken) {
    try {
      restClient.dropSign(new BitDropSignRequest(signProcessToken));
      log.debug("BIT sign process dropped: token={}", signProcessToken);
    } catch (Exception e) {
      log.warn("Failed to drop BIT sign process token={}: {}", signProcessToken, e.getMessage());
    }
  }
}
