package ch.agridata.agreement.service;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.DECLINED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.GRANTED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.OPENED;
import static ch.agridata.user.dto.LegalFormEnum.GMBH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.dto.ConsentRequestAggregationProducerView;
import ch.agridata.agreement.dto.ConsentRequestAggregationStateEnum;
import ch.agridata.agreement.dto.ConsentRequestProducerViewDto;
import ch.agridata.agreement.mapper.ConsentRequestMapper;
import ch.agridata.agreement.mapper.ConsentRequestMapperImpl;
import ch.agridata.agreement.mapper.DataRequestMapper;
import ch.agridata.agreement.persistence.ConsentRequestEntity;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.user.api.UserApi;
import ch.agridata.user.dto.UidDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsentRequestAggregationQueryServiceTest {
  @Mock
  private UserApi userApi;
  @Mock
  private AgridataSecurityIdentity identity;
  @Mock
  private ConsentRequestRepository consentRequestRepository;
  @Spy
  private final ConsentRequestMapper consentRequestMapper = new ConsentRequestMapperImpl();
  @Spy
  private final DataRequestMapper dataRequestMapper = Mappers.getMapper(DataRequestMapper.class);
  @InjectMocks
  private ConsentRequestAggregationQueryService service;

  private static final String AUTH_UID = "CHE000000001";
  private static final UUID DR1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID DR2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID CR1 = UUID.fromString("00000000-0000-0000-0000-000000000003");
  private static final UUID CR2 = UUID.fromString("00000000-0000-0000-0000-000000000004");

  @BeforeEach
  void setUp() {
    when(identity.getKtIdpOrImpersonatedKtIdP()).thenReturn("dummyp-kt-id-p-1");
    when(identity.getAgateLoginIdOrImpersonatedAgateLoginId()).thenReturn("agate_user_1");
    when(userApi.getAuthorizedUids("dummyp-kt-id-p-1", "agate_user_1"))
        .thenReturn(
            List.of(new UidDto(AUTH_UID, "Dummy Company 1", GMBH))
        );
  }

  @Test
  void givenAuthorizedUidAndNoConsentRequests_whenGetConsentRequestAggregationsAsCurrentDataProducer_thenReturnEmptyList() {
    when(consentRequestRepository.findByDataProducerUidsWithDataRequest(List.of(AUTH_UID)))
        .thenReturn(List.of());

    var result = service.getConsentRequestAggregationsAsCurrentDataProducer(AUTH_UID);
    assertThat(result).isEmpty();
  }

  @Test
  void givenUnauthorizedUid_whenGetConsentRequestAggregationsAsCurrentDataProducer_thenReturnEmptyList() {
    var result = service.getConsentRequestAggregationsAsCurrentDataProducer("CHE101000002");
    assertThat(result).isEmpty();
    verify(userApi, times(1)).getAuthorizedUids("dummyp-kt-id-p-1", "agate_user_1");
    verifyNoInteractions(consentRequestRepository);
  }

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
  void aggregationState_isDerivedFromConsentRequestStates(
      List<ConsentRequestEntity.StateEnum> states,
      ConsentRequestAggregationStateEnum expected
  ) {
    var dataRequest = dataRequest(DR1, LocalDateTime.of(2026, 1, 1, 0, 0));

    when(consentRequestRepository.findByDataProducerUidsWithDataRequest(List.of(AUTH_UID))).thenReturn(List.of(
        consentRequest(CR1, dataRequest, states.getFirst(), LocalDateTime.of(2026, 2, 1, 0, 0), LocalDateTime.of(2026, 3, 1, 0, 0), "BUR1",
            null),
        consentRequest(CR2, dataRequest, states.get(1), LocalDateTime.of(2026, 4, 1, 0, 0), LocalDateTime.of(2026, 5, 1, 0, 0), "BUR2",
            LocalDateTime.of(2026, 6, 1, 0, 0))
    ));

    var result = service.getConsentRequestAggregationsAsCurrentDataProducer(AUTH_UID);

    verify(consentRequestRepository).findByDataProducerUidsWithDataRequest(List.of(AUTH_UID));
    verifyNoMoreInteractions(consentRequestRepository);
    verify(userApi, times(1)).getAuthorizedUids("dummyp-kt-id-p-1", "agate_user_1");

    assertThat(result).hasSize(1);
    assertThat(result).extracting(ConsentRequestAggregationProducerView::id)
        .containsExactly(DR1);
    assertThat(result).singleElement().satisfies(a -> {
      assertThat(a.consentRequests()).hasSize(2);
      assertThat(a.requestDate()).isEqualTo(LocalDate.of(2026, 4, 1));
      assertThat(a.stateCode()).isEqualTo(expected);
      assertThat(a.lastStateChangeDate()).isEqualTo(LocalDateTime.of(2026, 5, 1, 0, 0));
      assertThat(a.dataProducerUid()).isEqualTo(AUTH_UID);
      assertThat(a.showStateAsMigrated()).isTrue();
      assertThat(a.consentRequests())
          .extracting(ConsentRequestProducerViewDto::id)
          .containsExactlyInAnyOrder(
              CR1,
              CR2
          );
    });
  }

  @Test
  void givenConsentRequestsWithoutLastStateChangeDate_whenGetConsentRequestAggregationsAsCurrentDataProducer_thenAggregationHasNullLastStateChangeDate() {
    var dataRequest = dataRequest(DR1, LocalDateTime.of(2026, 1, 1, 0, 0));

    when(consentRequestRepository.findByDataProducerUidsWithDataRequest(List.of(AUTH_UID))).thenReturn(List.of(
        consentRequest(CR1, dataRequest, GRANTED, LocalDateTime.of(2026, 2, 1, 0, 0), null, "BUR1", null)
    ));

    var result = service.getConsentRequestAggregationsAsCurrentDataProducer(AUTH_UID);

    verify(consentRequestRepository).findByDataProducerUidsWithDataRequest(List.of(AUTH_UID));
    verifyNoMoreInteractions(consentRequestRepository);
    verify(userApi, times(1)).getAuthorizedUids("dummyp-kt-id-p-1", "agate_user_1");

    assertThat(result).hasSize(1);
    assertThat(result).extracting(ConsentRequestAggregationProducerView::id)
        .containsExactly(DR1);
    assertThat(result).singleElement().satisfies(a -> {
      assertThat(a.consentRequests()).hasSize(1);
      assertThat(a.requestDate()).isEqualTo(LocalDate.of(2026, 2, 1));
      assertThat(a.stateCode()).isEqualTo(ConsentRequestAggregationStateEnum.GRANTED);
      assertThat(a.dataProducerUid()).isEqualTo(AUTH_UID);
      assertThat(a.showStateAsMigrated()).isFalse();
      assertThat(a.consentRequests())
          .extracting(ConsentRequestProducerViewDto::id)
          .containsExactlyInAnyOrder(
              CR1
          );
    });
  }

  @Test
  void givenConsentRequestsForMultipleDataRequests_whenGetConsentRequestAggregationsAsCurrentDataProducer_thenReturnAggregationsPerDataRequest() {
    var dataRequest1 = dataRequest(DR1, LocalDateTime.of(2026, 1, 1, 0, 0));
    var dataRequest2 = dataRequest(DR2, LocalDateTime.of(2026, 2, 1, 0, 0));

    when(consentRequestRepository.findByDataProducerUidsWithDataRequest(List.of(AUTH_UID))).thenReturn(List.of(
        consentRequest(CR1, dataRequest1, GRANTED, LocalDateTime.of(2026, 2, 1, 0, 0), LocalDateTime.of(2026, 3, 1, 0, 0), "BUR1",
            null),
        consentRequest(CR2, dataRequest2, DECLINED, LocalDateTime.of(2026, 4, 1, 0, 0), LocalDateTime.of(2026, 5, 1, 0, 0), "BUR2",
            LocalDateTime.of(2026, 6, 1, 0, 0))
    ));

    var result = service.getConsentRequestAggregationsAsCurrentDataProducer(AUTH_UID);

    verify(consentRequestRepository).findByDataProducerUidsWithDataRequest(List.of(AUTH_UID));
    verifyNoMoreInteractions(consentRequestRepository);
    verify(userApi, times(1)).getAuthorizedUids("dummyp-kt-id-p-1", "agate_user_1");

    assertThat(result)
        .extracting(ConsentRequestAggregationProducerView::id)
        .containsExactlyInAnyOrder(DR1, DR2);

    assertThat(result).satisfiesExactlyInAnyOrder(
        a -> {
          assertThat(a.id()).isEqualTo(DR1);
          assertThat(a.stateCode()).isEqualTo(ConsentRequestAggregationStateEnum.GRANTED);
          assertThat(a.lastStateChangeDate()).isEqualTo(LocalDateTime.of(2026, 3, 1, 0, 0));
          assertThat(a.dataProducerUid()).isEqualTo(AUTH_UID);
          assertThat(a.consentRequests()).hasSize(1);
          assertThat(a.consentRequests())
              .extracting(ConsentRequestProducerViewDto::id)
              .containsExactly(CR1);
        },
        a -> {
          assertThat(a.id()).isEqualTo(DR2);
          assertThat(a.stateCode()).isEqualTo(ConsentRequestAggregationStateEnum.DECLINED);
          assertThat(a.lastStateChangeDate()).isEqualTo(LocalDateTime.of(2026, 5, 1, 0, 0));
          assertThat(a.dataProducerUid()).isEqualTo(AUTH_UID);
          assertThat(a.consentRequests()).hasSize(1);
          assertThat(a.consentRequests())
              .extracting(ConsentRequestProducerViewDto::id)
              .containsExactly(CR2);
        }
    );
  }

  @Test
  void givenAllConsentRequestsMigratedFromMaf_whenGetConsentRequestAggregationsAsCurrentDataProducer_thenAggregationShowsStateAsMigrated() {
    var dataRequest = dataRequest(DR1, LocalDateTime.of(2026, 1, 1, 0, 0));

    when(consentRequestRepository.findByDataProducerUidsWithDataRequest(List.of(AUTH_UID))).thenReturn(List.of(
        consentRequest(CR1, dataRequest, GRANTED, LocalDateTime.of(2026, 2, 1, 0, 0), LocalDateTime.of(2026, 3, 1, 0, 0), "BUR1",
            LocalDateTime.of(2026, 6, 1, 0, 0)),
        consentRequest(CR2, dataRequest, DECLINED, LocalDateTime.of(2026, 4, 1, 0, 0), LocalDateTime.of(2026, 5, 1, 0, 0), "BUR2",
            LocalDateTime.of(2026, 6, 1, 0, 0))
    ));

    var aggregations = service.getConsentRequestAggregationsAsCurrentDataProducer(AUTH_UID);
    assertThat(aggregations).hasSize(1);
    assertThat(aggregations.getFirst().showStateAsMigrated()).isTrue();
  }

  @Test
  void givenNotAllConsentRequestsMigratedFromMaf_whenGetConsentRequestAggregationsAsCurrentDataProducer_thenAggregationShowsStateAsNotMigrated() {
    var dataRequest = dataRequest(DR1, LocalDateTime.of(2026, 1, 1, 0, 0));

    when(consentRequestRepository.findByDataProducerUidsWithDataRequest(List.of(AUTH_UID))).thenReturn(List.of(
        consentRequest(CR1, dataRequest, GRANTED, LocalDateTime.of(2026, 2, 1, 0, 0), LocalDateTime.of(2026, 3, 1, 0, 0), "BUR1",
            null),
        consentRequest(CR2, dataRequest, DECLINED, LocalDateTime.of(2026, 4, 1, 0, 0), LocalDateTime.of(2026, 5, 1, 0, 0), "BUR2",
            LocalDateTime.of(2026, 6, 1, 0, 0))
    ));

    var aggregations = service.getConsentRequestAggregationsAsCurrentDataProducer(AUTH_UID);
    assertThat(aggregations).hasSize(1);
    assertThat(aggregations.getFirst().showStateAsMigrated()).isTrue();
  }

  private static DataRequestEntity dataRequest(UUID id, LocalDateTime submissionDate) {
    return DataRequestEntity.builder().id(id).submissionDate(submissionDate).build();
  }

  private static ConsentRequestEntity consentRequest(
      UUID id,
      DataRequestEntity dataRequest,
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
        .dataProducerUid(AUTH_UID)
        .dataProducerBur(bur)
        .migratedFromMafDate(migratedFromMafDate)
        .dataRequest(dataRequest)
        .build();
  }
}
