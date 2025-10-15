package ch.agridata.datatransfer.service;

import static ch.agridata.common.filters.PreSecurityMdcFilter.REQUEST_ID_MDC_FIELD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.api.ConsentRequestApi;
import ch.agridata.datatransfer.dto.DataTransferResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

@ExtendWith(MockitoExtension.class)
class DataTransferServiceTest {

  private static final String BUR_ID = "A1234";
  private static final String UID = "CHE123456789";
  private static final UUID PRODUCT_ID = UUID.randomUUID();
  private static final UUID CONSENT_REQUEST_ID = UUID.randomUUID();
  private static final Object DATA = new Object();
  private static final Map<String, String> PARAMS = Map.of("year", "2025");

  @Mock
  ConsentRequestApi consentRequestApi;
  @Mock
  DataFetchingService dataFetchingService;
  @InjectMocks
  DataTransferService dataTransferService;

  @BeforeEach
  void setUp() {
    MDC.put(REQUEST_ID_MDC_FIELD, "test-request-id");
  }

  @Test
  void givenConsentGiven_whenTransferDataByBur_thenDataFetched() {
    when(consentRequestApi.getConsentRequestIdsOfCurrentConsumerGrantedByProducerForProductByBur(BUR_ID, PRODUCT_ID))
        .thenReturn(List.of(CONSENT_REQUEST_ID));
    when(dataFetchingService.fetchData(PRODUCT_ID, PARAMS)).thenReturn(DATA);

    var response = dataTransferService.transferDataByBur(PRODUCT_ID, BUR_ID, PARAMS);

    assertThat(response).isEqualTo(
        DataTransferResponse.builder()
            .data(DATA)
            .dataTransferRequestId("test-request-id")
            .consentRequestId(CONSENT_REQUEST_ID)
            .build()
    );
  }

  @Test
  void givenNoConsentGiven_whenTransferDataByBur_thenThrowException() {
    when(consentRequestApi.getConsentRequestIdsOfCurrentConsumerGrantedByProducerForProductByBur(BUR_ID, PRODUCT_ID))
        .thenReturn(Collections.emptyList());

    assertThatThrownBy(() -> dataTransferService.transferDataByBur(PRODUCT_ID, BUR_ID, PARAMS))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void givenConsentGiven_whenTransferDataByUid_thenDataFetched() {
    when(consentRequestApi.getConsentRequestIdsOfCurrentConsumerGrantedByProducerForProductByUid(UID, PRODUCT_ID))
        .thenReturn(List.of(CONSENT_REQUEST_ID));
    when(dataFetchingService.fetchData(PRODUCT_ID, PARAMS)).thenReturn(DATA);

    var response = dataTransferService.transferDataByUid(PRODUCT_ID, UID, PARAMS);

    assertThat(response).isEqualTo(
        DataTransferResponse.builder()
            .data(DATA)
            .dataTransferRequestId("test-request-id")
            .consentRequestId(CONSENT_REQUEST_ID)
            .build()
    );
  }

  @Test
  void givenNoConsentGiven_whenTransferDataByUid_thenThrowException() {
    when(consentRequestApi.getConsentRequestIdsOfCurrentConsumerGrantedByProducerForProductByUid(UID, PRODUCT_ID))
        .thenReturn(Collections.emptyList());

    assertThatThrownBy(() -> dataTransferService.transferDataByUid(PRODUCT_ID, UID, PARAMS))
        .isInstanceOf(IllegalArgumentException.class);
  }

}
