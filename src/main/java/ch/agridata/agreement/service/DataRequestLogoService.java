package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.NotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/**
 * Handles logo processing for data requests. It supports encoding and managing consumer branding.
 *
 * @CommentLastReviewed 2025-08-25
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataRequestLogoService {

  private static final List<String> ALLOWED_FILE_TYPES = List.of("image/jpeg", "image/jpg", "image/png");
  private static final int MAX_FILE_SIZE_KB = 100;

  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final DataRequestRepository dataRequestRepository;

  @Transactional
  @RolesAllowed(CONSUMER_ROLE)
  public void updateDataRequestLogo(UUID requestId, FileUpload logo) {
    var entity = dataRequestRepository.findByIdAndDataConsumerUid(requestId, agridataSecurityIdentity.getUidOrElseThrow())
        .orElseThrow(() -> new NotFoundException(requestId.toString()));

    if (!ALLOWED_FILE_TYPES.contains(logo.contentType())) {
      throw new ValidationException("File type not supported: " + logo.contentType());
    }
    if (logo.size() > MAX_FILE_SIZE_KB * 1024) {
      throw new ValidationException("File too large");
    }

    try {
      byte[] logoBytes = Files.readAllBytes(logo.uploadedFile());
      entity.setDataConsumerLogo(logoBytes);
      entity.setDataConsumerLogoType(logo.contentType());
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to read logo file", e);
    }
  }
}
