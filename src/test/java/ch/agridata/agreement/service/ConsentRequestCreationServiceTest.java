package ch.agridata.agreement.service;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.LEGALLY_PERMITTED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.OPENED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agis.api.AgisApi;
import ch.agridata.agreement.dto.ConsentRequestCreatedDto;
import ch.agridata.agreement.dto.CreateConsentRequestDto;
import ch.agridata.agreement.mapper.ConsentRequestMapper;
import ch.agridata.agreement.mapper.ConsentRequestMapperImpl;
import ch.agridata.agreement.persistence.ConsentRequestEntity;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import ch.agridata.agreement.persistence.DataRequestDataProductEntity;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.FlowCodeEnum;
import ch.agridata.user.api.UserApi;
import ch.agridata.user.dto.BurDto;
import ch.agridata.user.dto.UidDto;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsentRequestCreationServiceTest {

  static final String UID1 = "uid1";
  static final String UID2 = "uid2";
  static final String UID3 = "uid3";
  static final String BUR1 = "99910001";
  static final String BUR2 = "99910002";
  static final LocalDateTime RELATION_SINCE_1 = LocalDateTime.of(2020, 1, 1, 0, 0);
  static final LocalDateTime RELATION_SINCE_2 = LocalDateTime.of(2021, 6, 30, 12, 0);
  @Spy
  private final ConsentRequestMapper consentRequestMapper = new ConsentRequestMapperImpl();
  @Mock
  private ConsentRequestRepository consentRequestRepository;
  @Mock
  private DataRequestQueryService dataRequestQueryService;
  @Mock
  private AgridataSecurityIdentity agridataSecurityIdentity;
  @Mock
  private UserApi userApi;
  @Mock
  private AgisApi agisApi;
  @Mock
  private DataRequestRepository dataRequestRepository;
  @Mock
  private DataProductApi dataProductApi;
  @Mock
  private SessionFactory sessionFactory;
  @Mock
  private ConsentRequestSyncService consentRequestSyncService;
  @InjectMocks
  private ConsentRequestCreationService service;

  @BeforeEach
  void setup() {
    lenient().when(sessionFactory.fromTransaction(any())).thenAnswer(invocation -> {
      Function<Object, List<ConsentRequestCreatedDto>> transactionFunction = invocation.getArgument(0);
      return transactionFunction.apply(null);

    });
  }

  @Test
  void givenActiveDataRequestAndAuthorizedUids_whenCreateConsentRequests_thenCreateConsentRequestsTransactional() {
    // Given
    UUID dataProductId = UUID.randomUUID();
    UUID dataRequestId = UUID.randomUUID();
    String ktIdP = "test-kt-id-p";
    String agateLoginId = "test-agateLoginId";

    List<String> authorizedUids = List.of(UID1, UID2, UID3);
    List<CreateConsentRequestDto> createConsentRequestDtos =
        authorizedUids.stream().map(uid -> CreateConsentRequestDto.builder().dataRequestId(dataRequestId).uid(uid).build()).toList();
    List<UidDto> uidDtos = authorizedUids.stream().map(uid -> UidDto.builder().uid(uid).build()).toList();

    DataRequestEntity dataRequest = DataRequestEntity.builder()
        .id(dataRequestId)
        .stateCode(DataRequestEntity.DataRequestStateEnum.ACTIVE)
        .dataProducts(List.of(DataRequestDataProductEntity.builder().dataProductId(dataProductId).build()))
        .build();

    // Mock existing consent requests (uid2 already has one)
    ConsentRequestEntity existingConsentRequest = ConsentRequestEntity.builder()
        .dataProducerUid(UID2)
        .build();

    ConsentRequestCreatedDto dto1 = ConsentRequestCreatedDto.builder()
        .id(UUID.randomUUID())
        .dataProducerUid(UID1)
        .isCreated(true)
        .build();

    ConsentRequestCreatedDto dto2 = ConsentRequestCreatedDto.builder()
        .id(null)
        .dataProducerUid(UID2)
        .isCreated(false)
        .build();

    ConsentRequestCreatedDto dto3 = ConsentRequestCreatedDto.builder()
        .id(UUID.randomUUID())
        .dataProducerUid(UID3)
        .isCreated(true)
        .build();

    // Mock interactions
    when(dataRequestRepository.findByIdOptional(dataRequestId)).thenReturn(Optional.of(dataRequest));
    when(dataProductApi.getActiveProductsByIds(List.of(dataProductId)))
        .thenReturn(List.of(DataProductDto.builder().id(dataProductId).flowCode(FlowCodeEnum.UID_BASED_PRE_VALIDATION)
            .consentRequired(true).build()));
    when(agridataSecurityIdentity.getKtIdP()).thenReturn(ktIdP);
    when(agridataSecurityIdentity.getAgateLoginId()).thenReturn(agateLoginId);
    when(userApi.getAuthorizedUids(ktIdP, agateLoginId)).thenReturn(uidDtos);
    when(consentRequestRepository.findActiveUidAndBurBasedByDataRequestIdAndDataProducerUid(dataRequestId, UID1))
        .thenReturn(List.of());
    when(consentRequestRepository.findActiveUidAndBurBasedByDataRequestIdAndDataProducerUid(dataRequestId, UID2))
        .thenReturn(List.of(existingConsentRequest));
    when(consentRequestRepository.findActiveUidAndBurBasedByDataRequestIdAndDataProducerUid(dataRequestId, UID3))
        .thenReturn(List.of());
    doAnswer(invocation -> {
      ConsentRequestEntity e = invocation.getArgument(0);
      // behave based on e.getDataProducerUid()
      switch (e.getDataProducerUid()) {
        case UID1 -> e.setId(dto1.id());
        case UID3 -> e.setId(dto3.id());
        default -> throw new NotFoundException();
      }
      return null; // void method
    }).when(consentRequestRepository).persist(any(ConsentRequestEntity.class));

    // When
    List<ConsentRequestCreatedDto> result = service.createConsentRequests(createConsentRequestDtos);

    // Then
    assertThat(result)
        .hasSize(3)
        .containsExactlyInAnyOrder(dto1, dto2, dto3);
  }

  @Test
  void givenInactiveDataRequest_whenCreateConsentRequests_thenThrowIllegalStateException() {
    // Given
    UUID dataRequestId = UUID.randomUUID();
    DataRequestEntity inactiveDataRequest = DataRequestEntity.builder()
        .id(dataRequestId)
        .stateCode(DataRequestEntity.DataRequestStateEnum.DRAFT)
        .build();

    when(dataRequestRepository.findByIdOptional(dataRequestId)).thenReturn(Optional.of(inactiveDataRequest));
    when(userApi.getAuthorizedUids(any(), any())).thenReturn(List.of(UidDto.builder().uid("test").build()));

    // When & Then
    assertThatThrownBy(() -> service.createConsentRequests(
        List.of(CreateConsentRequestDto.builder().dataRequestId(dataRequestId).uid("test").build())))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(dataRequestId.toString());
  }

  @Test
  void givenNonExistentDataRequest_whenCreateConsentRequests_thenThrowNotFoundException() {
    // Given
    UUID dataRequestId = UUID.randomUUID();
    when(dataRequestRepository.findByIdOptional(dataRequestId)).thenReturn(Optional.empty());
    when(userApi.getAuthorizedUids(any(), any())).thenReturn(List.of(UidDto.builder().uid("test").build()));

    // When & Then
    assertThatThrownBy(() -> service.createConsentRequests(
        List.of(CreateConsentRequestDto.builder().dataRequestId(dataRequestId).uid("test").build()))
    )
        .isInstanceOf(NotFoundException.class)
        .hasMessage(dataRequestId.toString());
  }

  @Test
  void givenNoAccessTouid_whenCreateConsentRequests_thenThrowIllegalArgumentException() {
    // Given
    UUID dataRequestId = UUID.randomUUID();

    when(userApi.getAuthorizedUids(any(), any())).thenReturn(List.of());

    // When & Then
    assertThatThrownBy(() -> service.createConsentRequests(
        List.of(CreateConsentRequestDto.builder().dataRequestId(dataRequestId).uid("testuid").build())))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("testuid");

    verify(consentRequestRepository, never()).persist(any(ConsentRequestEntity.class));
    verify(userApi, never()).getAuthorizedBurs(anyString());
  }

  @Test
  void givenDataRequestWithoutBurProducts_whenCreateConsentRequests_thenOnlyUidConsentRequestIsCreated() {
    // Given
    var dataRequest = activeDataRequestWithProduct(FlowCodeEnum.UID_BASED_PRE_VALIDATION);
    givenAuthorizedProducer(dataRequest);

    // When
    service.createConsentRequests(
        List.of(CreateConsentRequestDto.builder().dataRequestId(dataRequest.getId()).uid(UID1).build()));

    // Then
    assertThat(persistedConsentRequests()).singleElement().satisfies(consentRequest -> {
      assertThat(consentRequest.getDataProducerUid()).isEqualTo(UID1);
      assertThat(consentRequest.getDataProducerBur()).isNull();
      assertThat(consentRequest.getStateCode()).isEqualTo(OPENED);
    });
    verify(userApi, never()).getAuthorizedBurs(anyString());
  }

  @ParameterizedTest
  @EnumSource(
      value = FlowCodeEnum.class, names = {
      "BUR_BASED_PRE_VALIDATION", "BUR_BASED_POST_VALIDATION", "UNBOUND_BUR_BASED_POST_VALIDATION"
  }
  )
  void givenDataRequestWithBurProduct_whenCreateConsentRequests_thenBurConsentRequestsAreCreated(FlowCodeEnum flowCode) {
    // Given
    var dataRequest = activeDataRequestWithProduct(flowCode);
    givenAuthorizedProducer(dataRequest);
    when(userApi.getAuthorizedBurs(UID1)).thenReturn(List.of(
        BurDto.builder().uid(UID1).bur(BUR1).relationSince(RELATION_SINCE_1).build(),
        BurDto.builder().uid(UID1).bur(BUR2).relationSince(RELATION_SINCE_2).build()
    ));

    // When
    var result = service.createConsentRequests(
        List.of(CreateConsentRequestDto.builder().dataRequestId(dataRequest.getId()).uid(UID1).build()));

    // Then
    assertThat(result).hasSize(3)
        .allSatisfy(dto -> assertThat(dto.dataProducerUid()).isEqualTo(UID1));

    assertThat(persistedConsentRequests()).hasSize(3);
    assertThat(persistedConsentRequests())
        .filteredOn(consentRequest -> consentRequest.getDataProducerBur() != null)
        .allSatisfy(consentRequest -> {
          assertThat(consentRequest.getDataProducerUid()).isEqualTo(UID1);
          assertThat(consentRequest.getUidBurRelationUntil()).isNull();
          assertThat(consentRequest.getStateCode()).isEqualTo(OPENED);
        })
        .extracting(ConsentRequestEntity::getDataProducerBur, ConsentRequestEntity::getUidBurRelationSince)
        .containsExactlyInAnyOrder(tuple(BUR1, RELATION_SINCE_1), tuple(BUR2, RELATION_SINCE_2));

    verify(consentRequestSyncService).syncUidConsentRequestStateWithBurConsentRequests(dataRequest.getId(), UID1);
  }

  @Test
  void givenExistingBurConsentRequest_whenCreateConsentRequests_thenOnlyMissingBurIsCreated() {
    // Given
    var dataRequest = activeDataRequestWithProduct(FlowCodeEnum.BUR_BASED_PRE_VALIDATION);
    givenAuthorizedProducer(dataRequest);
    when(userApi.getAuthorizedBurs(UID1)).thenReturn(List.of(
        BurDto.builder().uid(UID1).bur(BUR1).relationSince(RELATION_SINCE_1).build(),
        BurDto.builder().uid(UID1).bur(BUR2).relationSince(RELATION_SINCE_2).build()
    ));
    when(consentRequestRepository.findActiveUidAndBurBasedByDataRequestIdAndDataProducerUid(dataRequest.getId(), UID1))
        .thenReturn(List.of(
            ConsentRequestEntity.builder().dataProducerUid(UID1).build(),
            ConsentRequestEntity.builder().dataProducerUid(UID1).dataProducerBur(BUR1).build()
        ));

    // When
    var result = service.createConsentRequests(
        List.of(CreateConsentRequestDto.builder().dataRequestId(dataRequest.getId()).uid(UID1).build()));

    // Then
    assertThat(result).hasSize(3);
    assertThat(persistedConsentRequests()).singleElement().satisfies(consentRequest -> {
      assertThat(consentRequest.getDataProducerUid()).isEqualTo(UID1);
      assertThat(consentRequest.getDataProducerBur()).isEqualTo(BUR2);
      assertThat(consentRequest.getUidBurRelationSince()).isEqualTo(RELATION_SINCE_2);
    });
  }

  @Test
  void givenNoBursForUid_whenCreateConsentRequests_thenOnlyUidConsentRequestIsCreated() {
    // Given
    var dataRequest = activeDataRequestWithProduct(FlowCodeEnum.BUR_BASED_POST_VALIDATION);
    givenAuthorizedProducer(dataRequest);
    when(userApi.getAuthorizedBurs(UID1)).thenReturn(List.of());

    // When
    service.createConsentRequests(
        List.of(CreateConsentRequestDto.builder().dataRequestId(dataRequest.getId()).uid(UID1).build()));

    // Then
    assertThat(persistedConsentRequests()).singleElement()
        .satisfies(consentRequest -> assertThat(consentRequest.getDataProducerBur()).isNull());
  }

  @Test
  void givenDataRequestNotRequiringConsent_whenCreateConsentRequests_thenUidConsentRequestIsLegallyPermitted() {
    // Given
    var dataRequest = activeDataRequestWithProduct(FlowCodeEnum.UID_BASED_PRE_VALIDATION, false);
    givenAuthorizedProducer(dataRequest);

    // When
    service.createConsentRequests(
        List.of(CreateConsentRequestDto.builder().dataRequestId(dataRequest.getId()).uid(UID1).build()));

    // Then
    assertThat(persistedConsentRequests()).singleElement().satisfies(consentRequest -> {
      assertThat(consentRequest.getDataProducerUid()).isEqualTo(UID1);
      assertThat(consentRequest.getDataProducerBur()).isNull();
      assertThat(consentRequest.getStateCode()).isEqualTo(LEGALLY_PERMITTED);
    });
    verify(userApi, never()).getAuthorizedBurs(anyString());
    verify(consentRequestSyncService, never()).syncUidConsentRequestStateWithBurConsentRequests(any(), anyString());
  }

  @Test
  void givenBurDataRequestNotRequiringConsent_whenCreateConsentRequests_thenAllConsentRequestsAreLegallyPermitted() {
    // Given
    var dataRequest = activeDataRequestWithProduct(FlowCodeEnum.BUR_BASED_PRE_VALIDATION, false);
    givenAuthorizedProducer(dataRequest);
    when(userApi.getAuthorizedBurs(UID1)).thenReturn(List.of(
        BurDto.builder().uid(UID1).bur(BUR1).relationSince(RELATION_SINCE_1).build(),
        BurDto.builder().uid(UID1).bur(BUR2).relationSince(RELATION_SINCE_2).build()
    ));

    // When
    var result = service.createConsentRequests(
        List.of(CreateConsentRequestDto.builder().dataRequestId(dataRequest.getId()).uid(UID1).build()));

    // Then
    assertThat(result).hasSize(3);
    assertThat(persistedConsentRequests()).hasSize(3)
        .allSatisfy(consentRequest -> {
          assertThat(consentRequest.getDataProducerUid()).isEqualTo(UID1);
          assertThat(consentRequest.getStateCode()).isEqualTo(LEGALLY_PERMITTED);
        });
    verify(consentRequestSyncService).syncUidConsentRequestStateWithBurConsentRequests(dataRequest.getId(), UID1);
  }

  private DataRequestEntity activeDataRequestWithProduct(FlowCodeEnum flowCode) {
    return activeDataRequestWithProduct(flowCode, true);
  }

  private DataRequestEntity activeDataRequestWithProduct(FlowCodeEnum flowCode, boolean consentRequired) {
    var dataProductId = UUID.randomUUID();
    var dataProducts = new ArrayList<DataRequestDataProductEntity>();
    dataProducts.add(DataRequestDataProductEntity.builder().dataProductId(dataProductId).build());

    when(dataProductApi.getActiveProductsByIds(List.of(dataProductId)))
        .thenReturn(List.of(DataProductDto.builder().id(dataProductId).flowCode(flowCode).consentRequired(consentRequired).build()));

    return DataRequestEntity.builder()
        .id(UUID.randomUUID())
        .stateCode(DataRequestEntity.DataRequestStateEnum.ACTIVE)
        .dataProducts(dataProducts)
        .build();
  }

  private void givenAuthorizedProducer(DataRequestEntity dataRequest) {
    when(userApi.getAuthorizedUids(any(), any())).thenReturn(List.of(UidDto.builder().uid(UID1).build()));
    when(dataRequestRepository.findByIdOptional(dataRequest.getId())).thenReturn(Optional.of(dataRequest));
  }

  private List<ConsentRequestEntity> persistedConsentRequests() {
    var captor = ArgumentCaptor.forClass(ConsentRequestEntity.class);
    verify(consentRequestRepository, atLeast(0)).persist(captor.capture());
    return captor.getAllValues();
  }

}
