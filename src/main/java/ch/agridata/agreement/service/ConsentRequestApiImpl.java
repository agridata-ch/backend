package ch.agridata.agreement.service;

import ch.agridata.agreement.api.ConsentRequestApi;
import ch.agridata.agreement.dto.ConsentRequestFundamentalViewDto;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of {@link ConsentRequestApi}, delegating each call to the responsible internal service
 *
 * @CommentLastReviewed 2026-09-03
 */
@ApplicationScoped
@RequiredArgsConstructor
public class ConsentRequestApiImpl implements ConsentRequestApi {

  private final ConsentRequestLegallyPermittedService consentRequestLegallyPermittedService;
  private final ConsentRequestQueryService consentRequestQueryService;

  @Override
  public List<String> getGrantedConsentRequestUidsForProductOfCurrentConsumerSince(UUID productId, LocalDateTime since) {
    return consentRequestQueryService.getGrantedConsentRequestUidsForProductOfCurrentConsumerSince(productId, since);
  }

  @Override
  public List<ConsentRequestFundamentalViewDto> getGrantedConsentRequestsOfDataRequestsAndProducersUids(List<UUID> dataRequestIds,
                                                                                                        List<String> producerUids) {
    return consentRequestQueryService.getGrantedConsentRequestsOfDataRequestsAndProducersUids(dataRequestIds, producerUids);
  }

  @Override
  public List<ConsentRequestFundamentalViewDto> getGrantedConsentRequestsOfDataRequestsAndProducersBurs(List<UUID> dataRequestIds,
                                                                                                        List<String> producerBurs) {
    return consentRequestQueryService.getGrantedConsentRequestsOfDataRequestsAndProducersBurs(dataRequestIds, producerBurs);
  }

  @Override
  public void enqueueLegallyPermittedUidBasedConsentRequest(UUID dataRequestId, String producerUid) {
    consentRequestLegallyPermittedService.enqueueUidBased(dataRequestId, producerUid);
  }

  @Override
  public void enqueueLegallyPermittedBurBasedConsentRequest(UUID dataRequestId, String producerBur) {
    consentRequestLegallyPermittedService.enqueueBurBased(dataRequestId, producerBur);
  }
}
