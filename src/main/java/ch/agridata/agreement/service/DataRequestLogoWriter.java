package ch.agridata.agreement.service;

import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Persists a data consumer logo for an existing data request.
 *
 * <p>This service is responsible solely for writing an already validated and
 * encoded logo to a {@code DataRequestEntity}. It enforces ownership by
 * restricting access to the currently authenticated data consumer.</p>
 *
 * <p>The logo is stored as PNG binary data together with its MIME type.
 * Validation, decoding, resizing, and re-encoding of the image are expected
 * to be handled by the caller.</p>
 *
 * @CommentLastReviewed 2026-01-16
 * @see ch.agridata.agreement.persistence.DataRequestRepository
 * @see ch.agridata.common.security.AgridataSecurityIdentity
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataRequestLogoWriter {
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final DataRequestRepository dataRequestRepository;

  @Transactional
  public void store(UUID requestId, byte[] pngBytes) {
    if (pngBytes == null || pngBytes.length == 0) {
      throw new IllegalArgumentException("pngBytes must not be null/empty");
    }

    final var entity = dataRequestRepository
        .findByIdAndDataConsumerUid(requestId, agridataSecurityIdentity.getUidOrElseThrow())
        .orElseThrow(() -> new NotFoundException(requestId.toString()));
    entity.setDataConsumerLogo(pngBytes);
    entity.setDataConsumerLogoType("image/png");
  }
}
