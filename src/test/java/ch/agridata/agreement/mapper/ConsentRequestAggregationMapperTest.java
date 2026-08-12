package ch.agridata.agreement.mapper;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.DECLINED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.GRANTED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.OPENED;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.dto.ConsentRequestAggregationStateEnum;
import ch.agridata.agreement.dto.ConsentRequestAggregationSummaryDto;
import ch.agridata.agreement.dto.ConsentRequestProducerViewV2Dto;
import ch.agridata.agreement.dto.ConsentRequestStateEnum;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.DataRequestSummaryDto;
import ch.agridata.agreement.persistence.ConsentRequestEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link ConsentRequestAggregationMapper}, covering the aggregated state derivation, the request and last-state-change
 * dates, the migration flag, and the shape of the mapped consent request lists.
 *
 * @CommentLastReviewed 2026-08-13
 */
class ConsentRequestAggregationMapperTest {
  private final ConsentRequestAggregationMapper mapper = new ConsentRequestAggregationMapperImpl(new ConsentRequestMapperImpl());

  private static final String PRODUCER_UID = "CHE000000001";
  private static final UUID DATA_REQUEST_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID CR1 = UUID.fromString("00000000-0000-0000-0000-000000000003");
  private static final UUID CR2 = UUID.fromString("00000000-0000-0000-0000-000000000004");

  private static final DataRequestSummaryDto SUMMARY_DATA_REQUEST = DataRequestSummaryDto.builder().id(DATA_REQUEST_ID).build();
  private static final DataRequestDto DETAIL_DATA_REQUEST = DataRequestDto.builder().id(DATA_REQUEST_ID).build();

  static Stream<Arguments> aggregationCases() {
    return Stream.of(
        Arguments.of(List.of(GRANTED, GRANTED), ConsentRequestAggregationStateEnum.GRANTED),
        Arguments.of(List.of(DECLINED, DECLINED), ConsentRequestAggregationStateEnum.DECLINED),
        Arguments.of(List.of(OPENED, OPENED), ConsentRequestAggregationStateEnum.OPENED),
        Arguments.of(List.of(OPENED, GRANTED), ConsentRequestAggregationStateEnum.PARTIALLY_OPENED),
        Arguments.of(List.of(OPENED, DECLINED), ConsentRequestAggregationStateEnum.PARTIALLY_OPENED),
        Arguments.of(List.of(GRANTED, DECLINED), ConsentRequestAggregationStateEnum.PARTIALLY_GRANTED)
    );
  }

  @ParameterizedTest
  @MethodSource("aggregationCases")
  void stateCode_isDerivedFromConsentRequestStates(
      List<ConsentRequestEntity.StateEnum> states,
      ConsentRequestAggregationStateEnum expected
  ) {
    var group = List.of(
        consentRequest(CR1, states.getFirst(), dateTime(2), dateTime(3), "BUR1", null),
        consentRequest(CR2, states.get(1), dateTime(4), dateTime(5), "BUR2", dateTime(6))
    );

    var result = mapper.toConsentRequestAggregationSummaryDto(group, SUMMARY_DATA_REQUEST);

    assertThat(result.id()).isEqualTo(DATA_REQUEST_ID);
    assertThat(result.stateCode()).isEqualTo(expected);
    assertThat(result.requestDate()).isEqualTo(LocalDate.of(2026, 4, 1));
    assertThat(result.consentRequests())
        .extracting(ConsentRequestAggregationSummaryDto.ConsentRequestStateDto::id)
        .containsExactlyInAnyOrder(CR1, CR2);
  }

  @Test
  void summaryConsentRequests_carryIdAndState() {
    var group = List.of(consentRequest(CR1, GRANTED, dateTime(2), dateTime(3), "BUR1", null));

    var result = mapper.toConsentRequestAggregationSummaryDto(group, SUMMARY_DATA_REQUEST);

    assertThat(result.consentRequests())
        .singleElement()
        .satisfies(consentRequest -> {
          assertThat(consentRequest.id()).isEqualTo(CR1);
          assertThat(consentRequest.stateCode()).isEqualTo(ConsentRequestStateEnum.GRANTED);
        });
  }

  @Test
  void showStateAsMigrated_isTrue_whenAnyConsentRequestIsMigrated() {
    var group = List.of(
        consentRequest(CR1, GRANTED, dateTime(2), dateTime(3), "BUR1", null),
        consentRequest(CR2, GRANTED, dateTime(4), dateTime(5), "BUR2", dateTime(6))
    );

    assertThat(mapper.toConsentRequestAggregationSummaryDto(group, SUMMARY_DATA_REQUEST).showStateAsMigrated()).isTrue();
  }

  @Test
  void showStateAsMigrated_isFalse_whenNoConsentRequestIsMigrated() {
    var group = List.of(consentRequest(CR1, GRANTED, dateTime(2), dateTime(3), "BUR1", null));

    assertThat(mapper.toConsentRequestAggregationSummaryDto(group, SUMMARY_DATA_REQUEST).showStateAsMigrated()).isFalse();
  }

  @Test
  void detail_derivesStateRequestDateAndLatestStateChangeDate() {
    var group = List.of(
        consentRequest(CR1, GRANTED, dateTime(2), dateTime(3), "BUR1", null),
        consentRequest(CR2, DECLINED, dateTime(4), dateTime(5), null, dateTime(6))
    );

    var result = mapper.toConsentRequestAggregationDto(group, DETAIL_DATA_REQUEST);

    assertThat(result.id()).isEqualTo(DATA_REQUEST_ID);
    assertThat(result.dataRequest()).isEqualTo(DETAIL_DATA_REQUEST);
    assertThat(result.stateCode()).isEqualTo(ConsentRequestAggregationStateEnum.PARTIALLY_GRANTED);
    assertThat(result.requestDate()).isEqualTo(LocalDate.of(2026, 4, 1));
    assertThat(result.lastStateChangeDate()).isEqualTo(LocalDateTime.of(2026, 5, 1, 0, 0));
    assertThat(result.consentRequests())
        .extracting(ConsentRequestProducerViewV2Dto::id)
        .containsExactlyInAnyOrder(CR1, CR2);
  }

  @Test
  void detail_mapsConsentRequestFieldsToProducerView() {
    var group = List.of(consentRequest(CR1, GRANTED, dateTime(2), dateTime(3), "BUR1", null));

    var result = mapper.toConsentRequestAggregationDto(group, DETAIL_DATA_REQUEST);

    assertThat(result.consentRequests())
        .singleElement()
        .satisfies(consentRequest -> {
          assertThat(consentRequest.id()).isEqualTo(CR1);
          assertThat(consentRequest.dataProducerUid()).isEqualTo(PRODUCER_UID);
          assertThat(consentRequest.dataProducerBur()).isEqualTo("BUR1");
          assertThat(consentRequest.stateCode()).isEqualTo(ConsentRequestStateEnum.GRANTED);
          assertThat(consentRequest.requestDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        });
  }

  @Test
  void detail_lastStateChangeDate_isNullWhenNonePresent() {
    var group = List.of(consentRequest(CR1, GRANTED, dateTime(2), null, "BUR1", null));

    assertThat(mapper.toConsentRequestAggregationDto(group, DETAIL_DATA_REQUEST).lastStateChangeDate()).isNull();
  }

  private static LocalDateTime dateTime(int month) {
    return LocalDateTime.of(2026, month, 1, 0, 0);
  }

  private static ConsentRequestEntity consentRequest(
      UUID id,
      ConsentRequestEntity.StateEnum state,
      LocalDateTime requestDate,
      LocalDateTime lastStateChangeDate,
      String bur,
      LocalDateTime migratedFromMafDate
  ) {
    return ConsentRequestEntity.builder()
        .id(id)
        .requestDate(requestDate)
        .stateCode(state)
        .lastStateChangeDate(lastStateChangeDate)
        .dataProducerUid(PRODUCER_UID)
        .dataProducerBur(bur)
        .migratedFromMafDate(migratedFromMafDate)
        .build();
  }
}
