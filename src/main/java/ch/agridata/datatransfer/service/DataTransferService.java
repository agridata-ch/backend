package ch.agridata.datatransfer.service;

import static ch.agridata.common.filters.PreSecurityMdcFilter.REQUEST_ID_MDC_FIELD;

import ch.agridata.agreement.api.ConsentRequestApi;
import ch.agridata.datatransfer.dto.DataTransferResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.MDC;

/**
 * Orchestrates data transfer workflows. It ensures consent is granted, invokes data fetching, and constructs standardized responses
 * containing the transferred data and identifiers.
 *
 * @CommentLastReviewed 2025-08-25
 */
@ApplicationScoped
@RequiredArgsConstructor
public class DataTransferService {

  private final ConsentRequestApi consentRequestApi;
  private final DataFetchingService dataFetchingService;

  public DataTransferResponse transferDataByBur(@NotNull @Valid UUID productId,
                                                @NotNull @Valid String bur,
                                                Map<String, String> params) {
    var consentRequestIds = consentRequestApi.getConsentRequestIdsOfCurrentConsumerGrantedByProducerForProductByBur(bur, productId);
    if (consentRequestIds.isEmpty()) {
      throw new IllegalArgumentException(
          "no consent for bur: " + bur + " and productId: " + productId + " found");
    }

    var data = dataFetchingService.fetchData(productId, params);

    return buildResponse(data, consentRequestIds.getFirst());
  }

  public DataTransferResponse transferDataByUid(@NotNull @Valid UUID productId,
                                                @NotNull @Valid String uid,
                                                Map<String, String> params) {
    var consentRequestIds = consentRequestApi.getConsentRequestIdsOfCurrentConsumerGrantedByProducerForProductByUid(uid, productId);
    if (consentRequestIds.isEmpty()) {
      throw new IllegalArgumentException(
          "no consent for uid: " + uid + " and productId: " + productId + " found");
    }

    var data = dataFetchingService.fetchData(productId, params);

    return buildResponse(data, consentRequestIds.getFirst());
  }

  private DataTransferResponse buildResponse(Object data, UUID consentRequestId) {
    return DataTransferResponse.builder()
        .data(data)
        .dataTransferRequestId(Optional.ofNullable(MDC.get(REQUEST_ID_MDC_FIELD)).map(Object::toString).orElse(null))
        .consentRequestId(consentRequestId)
        .build();
  }

}
