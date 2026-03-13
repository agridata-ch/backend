package ch.agridata.datatransferv2.service;

import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import jakarta.ws.rs.core.Response;
import java.util.Map;

/**
 * Interface for data transfer flow implementations. Each flow defines how to process a data transfer request.
 *
 * @CommentLastReviewed 2026-02-26
 */
public interface Flowable {

  Response run(DataProductProviderConfigurationDto productProviderConfiguration,
               Map<String, String> queryParameters);

}
