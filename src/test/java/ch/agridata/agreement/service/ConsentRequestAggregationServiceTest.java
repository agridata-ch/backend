package ch.agridata.agreement.service;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.GRANTED;
import static ch.agridata.user.dto.LegalFormEnum.GMBH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.dto.ConsentRequestAggregationDto;
import ch.agridata.agreement.dto.ConsentRequestAggregationSummaryDto;
import ch.agridata.agreement.dto.ConsentRequestProducerViewV2Dto;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.mapper.ConsentRequestAggregationMapper;
import ch.agridata.agreement.mapper.ConsentRequestAggregationMapperImpl;
import ch.agridata.agreement.mapper.ConsentRequestMapperImpl;
import ch.agridata.agreement.mapper.DataRequestMapper;
import ch.agridata.agreement.mapper.DataRequestMapperImpl;
import ch.agridata.agreement.persistence.ConsentRequestEntity;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.user.api.UserApi;
import ch.agridata.user.dto.UidDto;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsentRequestAggregationServiceTest {
  @Mock
  private UserApi userApi;
  @Mock
  private AgridataSecurityIdentity identity;
  @Mock
  private ConsentRequestRepository consentRequestRepository;
  @Mock
  private DataRequestEnrichmentService dataRequestEnrichmentService;
  @Spy
  private final ConsentRequestAggregationMapper aggregationMapper = new ConsentRequestAggregationMapperImpl(new ConsentRequestMapperImpl());
  @Spy
  private final DataRequestMapper dataRequestMapper = new DataRequestMapperImpl();
  @InjectMocks
  private ConsentRequestAggregationService service;

  private static final String AUTH_UID = "CHE000000001";
  private static final String UNAUTHORIZED_UID = "CHE101000002";
  private static final UUID DR1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID DR2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID CR1 = UUID.fromString("00000000-0000-0000-0000-000000000003");
  private static final UUID CR2 = UUID.fromString("00000000-0000-0000-0000-000000000004");

  @BeforeEach
  void setUp() {
    when(identity.getKtIdpOrImpersonatedKtIdP()).thenReturn("dummyp-kt-id-p-1");
    when(identity.getAgateLoginIdOrImpersonatedAgateLoginId()).thenReturn("agate_user_1");
    when(userApi.getAuthorizedUids("dummyp-kt-id-p-1", "agate_user_1"))
        .thenReturn(List.of(new UidDto(AUTH_UID, "Dummy Company 1", GMBH)));
  }

  @Test
  void givenUnauthorizedUid_whenGetAggregations_thenReturnEmptyListWithoutRepositoryAccess() {
    var result = service.getConsentRequestAggregationsAsCurrentDataProducer(UNAUTHORIZED_UID);

    assertThat(result).isEmpty();
    verifyNoInteractions(consentRequestRepository);
  }

  @Test
  void givenAuthorizedUidAndNoConsentRequests_whenGetAggregations_thenReturnEmptyList() {
    when(consentRequestRepository.findByDataProducerUidsWithDataRequest(List.of(AUTH_UID))).thenReturn(List.of());

    assertThat(service.getConsentRequestAggregationsAsCurrentDataProducer(AUTH_UID)).isEmpty();
  }

  @Test
  void givenConsentRequestsForMultipleDataRequests_whenGetAggregations_thenReturnOneAggregationPerDataRequest() {
    when(consentRequestRepository.findByDataProducerUidsWithDataRequest(List.of(AUTH_UID))).thenReturn(List.of(
        consentRequest(CR1, dataRequest(DR1), dateTime(2)),
        consentRequest(CR2, dataRequest(DR2), dateTime(4))
    ));

    var result = service.getConsentRequestAggregationsAsCurrentDataProducer(AUTH_UID);

    assertThat(result)
        .extracting(ConsentRequestAggregationSummaryDto::id)
        .containsExactlyInAnyOrder(DR1, DR2);
  }

  @Test
  void givenAggregations_whenGetAggregations_thenSortedByRequestDateDescendingThenById() {
    var dataRequestA = dataRequest(DR1);
    var dataRequestB = dataRequest(DR2);
    when(consentRequestRepository.findByDataProducerUidsWithDataRequest(List.of(AUTH_UID))).thenReturn(List.of(
        // DR1 latest request date February, DR2 latest request date April
        consentRequest(CR1, dataRequestA, dateTime(2)),
        consentRequest(CR2, dataRequestB, dateTime(4))
    ));

    var result = service.getConsentRequestAggregationsAsCurrentDataProducer(AUTH_UID);

    assertThat(result)
        .extracting(ConsentRequestAggregationSummaryDto::id)
        .containsExactly(DR2, DR1);
  }

  @Test
  void givenAggregationsWithEqualRequestDate_whenGetAggregations_thenSortedById() {
    var dataRequestA = dataRequest(DR1);
    var dataRequestB = dataRequest(DR2);
    when(consentRequestRepository.findByDataProducerUidsWithDataRequest(List.of(AUTH_UID))).thenReturn(List.of(
        consentRequest(CR2, dataRequestB, dateTime(2)),
        consentRequest(CR1, dataRequestA, dateTime(2))
    ));

    var result = service.getConsentRequestAggregationsAsCurrentDataProducer(AUTH_UID);

    assertThat(result)
        .extracting(ConsentRequestAggregationSummaryDto::id)
        .containsExactly(DR1, DR2);
  }

  @Test
  void givenUnauthorizedUid_whenGetAggregation_thenThrowNotFoundWithoutRepositoryAccess() {
    assertThatThrownBy(() -> service.getConsentRequestAggregationAsCurrentDataProducer(UNAUTHORIZED_UID, DR1))
        .isInstanceOf(NotFoundException.class);
    verifyNoInteractions(consentRequestRepository);
  }

  @Test
  void givenNoConsentRequestsForDataRequest_whenGetAggregation_thenThrowNotFound() {
    when(consentRequestRepository.findByDataRequestIdAndDataProducerUids(DR1, List.of(AUTH_UID))).thenReturn(List.of());

    assertThatThrownBy(() -> service.getConsentRequestAggregationAsCurrentDataProducer(AUTH_UID, DR1))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void givenAuthorizedUidAndConsentRequests_whenGetAggregation_thenReturnAggregationWithEnrichedDataRequest() {
    var dataRequest = dataRequest(DR1);
    var enrichedDataRequest = DataRequestDto.builder().id(DR1).build();
    when(consentRequestRepository.findByDataRequestIdAndDataProducerUids(DR1, List.of(AUTH_UID)))
        .thenReturn(List.of(consentRequest(CR1, dataRequest, dateTime(2))));
    when(dataRequestEnrichmentService.toEnrichedDto(dataRequest)).thenReturn(enrichedDataRequest);

    ConsentRequestAggregationDto result = service.getConsentRequestAggregationAsCurrentDataProducer(AUTH_UID, DR1);

    assertThat(result.id()).isEqualTo(DR1);
    assertThat(result.dataRequest()).isEqualTo(enrichedDataRequest);
    assertThat(result.consentRequests())
        .extracting(ConsentRequestProducerViewV2Dto::id)
        .containsExactly(CR1);
    verify(dataRequestEnrichmentService).toEnrichedDto(dataRequest);
  }

  private static DataRequestEntity dataRequest(UUID id) {
    return DataRequestEntity.builder().id(id).build();
  }

  private static LocalDateTime dateTime(int month) {
    return LocalDateTime.of(2026, month, 1, 0, 0);
  }

  private static ConsentRequestEntity consentRequest(UUID id, DataRequestEntity dataRequest, LocalDateTime requestDate) {
    return ConsentRequestEntity.builder()
        .id(id)
        .requestDate(requestDate)
        .stateCode(GRANTED)
        .dataProducerUid(AUTH_UID)
        .dataProducerBur("BUR")
        .dataRequest(dataRequest)
        .build();
  }
}
