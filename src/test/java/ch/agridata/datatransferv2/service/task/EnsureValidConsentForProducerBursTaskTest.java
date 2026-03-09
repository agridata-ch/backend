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
import com.google.common.collect.Range;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
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
  private static final LocalDate REQUESTED_RANGE_FROM = LocalDate.of(2025, 12, 1);
  private static final LocalDate REQUESTED_RANGE_UNTIL = LocalDate.of(2026, 1, 15);
  private static final LocalDate RANGE_UNTIL_MAX = LocalDate.of(9999, 12, 31);
  private static final Range<@NotNull LocalDate> REQUESTED_DATE_RANGE = Range.closed(REQUESTED_RANGE_FROM, REQUESTED_RANGE_UNTIL);

  @Mock
  ConsentRequestApi consentRequestApi;

  @InjectMocks
  EnsureValidConsentForProducerBursTask task;

  @Test
  void givenNoConsentGranted_whenApply_thenConsentNotGrantedExceptionIsThrown() {
    var context = createContextWithProducerBurs(List.of(PRODUCER_BUR_1));

    when(consentRequestApi.getGrantedConsentRequestsOfDataRequestAndProducersBurs(
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
    var consent = createConsent(PRODUCER_BUR_1, REQUESTED_RANGE_FROM.minusDays(30), RANGE_UNTIL_MAX);

    when(consentRequestApi.getGrantedConsentRequestsOfDataRequestAndProducersBurs(
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
    var consent1 = createConsent(PRODUCER_BUR_1, REQUESTED_RANGE_FROM.minusDays(30), RANGE_UNTIL_MAX);
    var consent2 = createConsent(PRODUCER_BUR_2, REQUESTED_RANGE_FROM.minusDays(60), RANGE_UNTIL_MAX);

    when(consentRequestApi.getGrantedConsentRequestsOfDataRequestAndProducersBurs(
        eq(DATA_REQUEST_ID), any()))
        .thenReturn(List.of(consent1, consent2));

    assertThat(task.apply(context)).isSameAs(context);
  }

  @ParameterizedTest(name = "[{0}] should be valid={1}")
  @MethodSource("consentRangeCoverageCases")
  void givenConsentRanges_whenApply_thenCoverageIsChecked(@SuppressWarnings("unused") String description,
                                                          boolean expectValid,
                                                          List<LocalDate[]> consentRanges) {
    var context = createContextWithProducerBurs(List.of(PRODUCER_BUR_1));
    var consents = consentRanges.stream()
        .map(range -> createConsent(PRODUCER_BUR_1, range[0], range[1]))
        .toList();

    when(consentRequestApi.getGrantedConsentRequestsOfDataRequestAndProducersBurs(
        eq(DATA_REQUEST_ID), any()))
        .thenReturn(consents);

    if (expectValid) {
      assertThat(task.apply(context)).isSameAs(context);
    } else {
      assertThatThrownBy(() -> task.apply(context))
          .isInstanceOf(ConsentNotGrantedException.class);
    }
  }

  static Stream<Arguments> consentRangeCoverageCases() {
    return Stream.of(
        Arguments.of("single: from before range, max until", true,
            ranges(r(REQUESTED_RANGE_FROM.minusDays(1), RANGE_UNTIL_MAX))),
        Arguments.of("single: from on range start, until after range end", true,
            ranges(r(REQUESTED_RANGE_FROM, REQUESTED_RANGE_UNTIL.plusDays(1)))),
        Arguments.of("single: from before range, until on range end", true,
            ranges(r(REQUESTED_RANGE_FROM.minusDays(1), REQUESTED_RANGE_UNTIL))),
        Arguments.of("single: from after range start", false,
            ranges(r(REQUESTED_RANGE_FROM.plusDays(1), RANGE_UNTIL_MAX))),
        Arguments.of("single: until before range end", false,
            ranges(r(REQUESTED_RANGE_FROM.minusDays(1), REQUESTED_RANGE_UNTIL.minusDays(1)))),
        Arguments.of("single: no from (null)", false,
            ranges(r(null, null))),
        Arguments.of("two consents: adjacent, no gap", true,
            ranges(r(REQUESTED_RANGE_FROM.minusDays(10), REQUESTED_RANGE_FROM.plusDays(20)),
                r(REQUESTED_RANGE_FROM.plusDays(21), RANGE_UNTIL_MAX))),
        Arguments.of("two consents: one-day gap in middle", false,
            ranges(r(REQUESTED_RANGE_FROM.minusDays(10), REQUESTED_RANGE_FROM.plusDays(20)),
                r(REQUESTED_RANGE_FROM.plusDays(22), RANGE_UNTIL_MAX))),
        Arguments.of("two consents: overlapping, together cover range", true,
            ranges(r(REQUESTED_RANGE_FROM.minusDays(5), REQUESTED_RANGE_FROM.plusDays(25)),
                r(REQUESTED_RANGE_FROM.plusDays(20), RANGE_UNTIL_MAX))),
        Arguments.of("single: gap at start (from one day too late)", false,
            ranges(r(REQUESTED_RANGE_FROM.plusDays(1), RANGE_UNTIL_MAX))),
        Arguments.of("single: gap at end (until one day too early)", false,
            ranges(r(REQUESTED_RANGE_FROM.minusDays(1), REQUESTED_RANGE_UNTIL.minusDays(1)))),
        Arguments.of("single: exact match of requested range", true,
            ranges(r(REQUESTED_RANGE_FROM, REQUESTED_RANGE_UNTIL))),
        Arguments.of("three consents: chained, no gap, partially overlapping", true,
            ranges(r(REQUESTED_RANGE_FROM.minusDays(5), REQUESTED_RANGE_FROM.plusDays(10)),
                r(REQUESTED_RANGE_FROM.plusDays(9), REQUESTED_RANGE_FROM.plusDays(25)),
                r(REQUESTED_RANGE_FROM.plusDays(26), RANGE_UNTIL_MAX))),
        Arguments.of("consent ends before range starts", false,
            ranges(r(REQUESTED_RANGE_FROM.minusDays(30), REQUESTED_RANGE_FROM.minusDays(1)))));
  }

  private static LocalDate[] r(LocalDate from, LocalDate until) {
    return new LocalDate[] {from, until};
  }

  private static List<LocalDate[]> ranges(LocalDate[]... ranges) {
    return Arrays.asList(ranges);
  }

  private AgridataContext createContextWithProducerBurs(List<String> producerBurs) {
    return AgridataContext.builder()
        .productId(PRODUCT_ID)
        .flowEnum(FlowEnum.BUR_BASED_POST_VALIDATION)
        .producerBurs(producerBurs)
        .validDataRequestIds(List.of(DATA_REQUEST_ID))
        .requestedDateRange(REQUESTED_DATE_RANGE)
        .build();
  }

  private ConsentRequestFundamentalViewDto createConsent(String producerBur, LocalDate from, LocalDate until) {
    return ConsentRequestFundamentalViewDto.builder()
        .id(CONSENT_REQUEST_ID)
        .dataRequestId(DATA_REQUEST_ID)
        .dataProducerBur(producerBur)
        .grantedDataPeriodFrom(from)
        .grantedDataPeriodTo(until)
        .stateCode(ConsentRequestStateEnum.GRANTED)
        .build();
  }
}
