package ch.agridata.datatransferv2.service.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.api.ConsentRequestApi;
import ch.agridata.agreement.dto.ConsentRequestFundamentalViewDto;
import ch.agridata.agreement.dto.ConsentRequestStateEnum;
import ch.agridata.common.exceptions.ConsentNotGrantedException;
import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.datatransferv2.service.FlowEnum;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnsureValidConsentForProducerUidsTaskTest {

  private static final UUID PRODUCT_ID = UUID.randomUUID();
  private static final UUID DATA_REQUEST_ID = UUID.randomUUID();
  private static final UUID CONSENT_REQUEST_ID = UUID.randomUUID();
  private static final String PRODUCER_UID_1 = "CHE111111111";
  private static final String PRODUCER_UID_2 = "CHE222222222";

  @Mock
  ConsentRequestApi consentRequestApi;

  @InjectMocks
  EnsureValidConsentForProducerUidsTask task;

  @Test
  void givenConsentGrantedForProducer_whenApply_thenContextIsReturned() {
    var context = createContextWithProducers(List.of(PRODUCER_UID_1));
    var consent = createConsent(PRODUCER_UID_1);

    when(consentRequestApi.getGrantedConsentRequestIdsOfDataRequestAndProducers(
        eq(DATA_REQUEST_ID), any()))
        .thenReturn(List.of(consent));

    var result = task.apply(context);

    assertThat(result).isSameAs(context);
  }

  @Test
  void givenNoConsentGranted_whenApply_thenConsentNotGrantedExceptionIsThrown() {
    var context = createContextWithProducers(List.of(PRODUCER_UID_1));

    when(consentRequestApi.getGrantedConsentRequestIdsOfDataRequestAndProducers(
        eq(DATA_REQUEST_ID), any()))
        .thenReturn(List.of());

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ConsentNotGrantedException.class)
        .hasMessageContaining("Consent not granted")
        .satisfies(ex -> {
          var cex = (ConsentNotGrantedException) ex;
          assertThat(cex.getMissingConsentUids()).contains(PRODUCER_UID_1);
        });
  }

  @Test
  void givenPartialConsent_whenApply_thenExceptionContainsMissingUids() {
    var context = createContextWithProducers(List.of(PRODUCER_UID_1, PRODUCER_UID_2));
    var consent = createConsent(PRODUCER_UID_1);

    when(consentRequestApi.getGrantedConsentRequestIdsOfDataRequestAndProducers(
        eq(DATA_REQUEST_ID), any()))
        .thenReturn(List.of(consent));

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ConsentNotGrantedException.class)
        .satisfies(ex -> {
          var cex = (ConsentNotGrantedException) ex;
          assertThat(cex.getMissingConsentUids()).containsExactly(PRODUCER_UID_2);
        });
  }

  @Test
  void givenMultipleProducersWithConsent_whenApply_thenSuccess() {
    var context = createContextWithProducers(List.of(PRODUCER_UID_1, PRODUCER_UID_2));
    var consent1 = createConsent(PRODUCER_UID_1);
    var consent2 = createConsent(PRODUCER_UID_2);

    when(consentRequestApi.getGrantedConsentRequestIdsOfDataRequestAndProducers(
        eq(DATA_REQUEST_ID), any()))
        .thenReturn(List.of(consent1, consent2));

    var result = task.apply(context);

    assertThat(result).isSameAs(context);
  }

  private AgridataContext createContextWithProducers(List<String> producerUids) {
    return AgridataContext.builder()
        .productId(PRODUCT_ID)
        .flowEnum(FlowEnum.UID_BASED_PRE_VALIDATION)
        .producerUidsInPayload(producerUids)
        .validDataRequestIds(List.of(DATA_REQUEST_ID))
        .build();
  }

  private ConsentRequestFundamentalViewDto createConsent(String producerUid) {
    return ConsentRequestFundamentalViewDto.builder()
        .id(CONSENT_REQUEST_ID)
        .dataRequestId(DATA_REQUEST_ID)
        .dataProducerUid(producerUid)
        .stateCode(ConsentRequestStateEnum.GRANTED)
        .build();
  }
}
