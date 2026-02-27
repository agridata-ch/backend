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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnsureValidConsentForProducerBursTaskTest {

  private static final UUID PRODUCT_ID = UUID.randomUUID();
  private static final UUID DATA_REQUEST_ID = UUID.randomUUID();
  private static final UUID CONSENT_REQUEST_ID = UUID.randomUUID();
  private static final String PRODUCER_BUR_1 = "A123456";
  private static final String PRODUCER_BUR_2 = "B789012";
  private static final LocalDate REQUESTED_DATE = LocalDate.of(2026, 1, 15);

  @Mock
  ConsentRequestApi consentRequestApi;

  @InjectMocks
  EnsureValidConsentForProducerBursTask task;

  @Test
  void givenConsentGrantedForProducer_whenApply_thenContextIsReturned() {
    var context = createContextWithProducerBurs(List.of(PRODUCER_BUR_1));
    var consent = createConsent(PRODUCER_BUR_1, REQUESTED_DATE.minusDays(30), null);

    when(consentRequestApi.getGrantedConsentRequestIdsOfDataRequestAndProducersBurs(
        eq(DATA_REQUEST_ID), any()))
        .thenReturn(List.of(consent));

    var result = task.apply(context);

    assertThat(result).isSameAs(context);
  }

  @Test
  void givenNoConsentGranted_whenApply_thenConsentNotGrantedExceptionIsThrown() {
    var context = createContextWithProducerBurs(List.of(PRODUCER_BUR_1));

    when(consentRequestApi.getGrantedConsentRequestIdsOfDataRequestAndProducersBurs(
        eq(DATA_REQUEST_ID), any()))
        .thenReturn(List.of());

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ConsentNotGrantedException.class)
        .hasMessageContaining("Consent not granted")
        .satisfies(ex -> {
          var cex = (ConsentNotGrantedException) ex;
          assertThat(cex.getProducerIdentifiers()).contains(PRODUCER_BUR_1);
        });
  }

  @Test
  void givenPartialConsent_whenApply_thenExceptionContainsMissingBurs() {
    var context = createContextWithProducerBurs(List.of(PRODUCER_BUR_1, PRODUCER_BUR_2));
    var consent = createConsent(PRODUCER_BUR_1, REQUESTED_DATE.minusDays(30), null);

    when(consentRequestApi.getGrantedConsentRequestIdsOfDataRequestAndProducersBurs(
        eq(DATA_REQUEST_ID), any()))
        .thenReturn(List.of(consent));

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ConsentNotGrantedException.class)
        .satisfies(ex -> {
          var cex = (ConsentNotGrantedException) ex;
          assertThat(cex.getProducerIdentifiers()).containsExactly(PRODUCER_BUR_2);
        });
  }

  @Test
  void givenMultipleProducersWithConsent_whenApply_thenSuccess() {
    var context = createContextWithProducerBurs(List.of(PRODUCER_BUR_1, PRODUCER_BUR_2));
    var consent1 = createConsent(PRODUCER_BUR_1, REQUESTED_DATE.minusDays(30), null);
    var consent2 = createConsent(PRODUCER_BUR_2, REQUESTED_DATE.minusDays(60), null);

    when(consentRequestApi.getGrantedConsentRequestIdsOfDataRequestAndProducersBurs(
        eq(DATA_REQUEST_ID), any()))
        .thenReturn(List.of(consent1, consent2));

    var result = task.apply(context);

    assertThat(result).isSameAs(context);
  }

  @ParameterizedTest(name = "relation [{0}] should be valid={1}")
  @MethodSource("consentDateValidityCases")
  void givenConsentWithUidBurRelationDateRange_whenApply_thenValidityIsChecked(String description,
                                                                               boolean expectValid,
                                                                               LocalDate relationSince,
                                                                               LocalDate relationUntil) {
    var context = createContextWithProducerBurs(List.of(PRODUCER_BUR_1));
    var consent = createConsent(PRODUCER_BUR_1, relationSince, relationUntil);

    when(consentRequestApi.getGrantedConsentRequestIdsOfDataRequestAndProducersBurs(
        eq(DATA_REQUEST_ID), any()))
        .thenReturn(List.of(consent));

    if (expectValid) {
      var result = task.apply(context);
      assertThat(result).isSameAs(context);
    } else {
      assertThatThrownBy(() -> task.apply(context))
          .isInstanceOf(ConsentNotGrantedException.class);
    }
  }

  static Stream<Arguments> consentDateValidityCases() {
    return Stream.of(
        Arguments.of("since before, no until", true, REQUESTED_DATE.minusDays(30), null),
        Arguments.of("since on requested date", true, REQUESTED_DATE, REQUESTED_DATE.plusDays(30)),
        Arguments.of("until on requested date", true, REQUESTED_DATE.minusDays(30), REQUESTED_DATE),
        Arguments.of("since after requested date", false, REQUESTED_DATE.plusDays(1), null),
        Arguments.of("until before requested date", false, REQUESTED_DATE.minusDays(60), REQUESTED_DATE.minusDays(1)),
        Arguments.of("no since, no until", false, null, null));
  }

  private AgridataContext createContextWithProducerBurs(List<String> producerBurs) {
    return AgridataContext.builder()
        .productId(PRODUCT_ID)
        .flowEnum(FlowEnum.BUR_BASED_POST_VALIDATION)
        .producerBurs(producerBurs)
        .validDataRequestIds(List.of(DATA_REQUEST_ID))
        .requestedDate(REQUESTED_DATE)
        .build();
  }

  private ConsentRequestFundamentalViewDto createConsent(String producerBur, LocalDate relationSince,
                                                         LocalDate relationUntil) {
    return ConsentRequestFundamentalViewDto.builder()
        .id(CONSENT_REQUEST_ID)
        .dataRequestId(DATA_REQUEST_ID)
        .dataProducerBur(producerBur)
        .uidBurRelationSince(relationSince != null ? relationSince.atStartOfDay() : null)
        .uidBurRelationUntil(relationUntil != null ? relationUntil.atStartOfDay() : null)
        .stateCode(ConsentRequestStateEnum.GRANTED)
        .build();
  }
}
