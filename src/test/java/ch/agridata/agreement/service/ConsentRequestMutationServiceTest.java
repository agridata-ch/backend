package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agis.api.AgisApi;
import ch.agridata.agreement.dto.ConsentRequestCreatedDto;
import ch.agridata.agreement.mapper.ConsentRequestMapper;
import ch.agridata.agreement.mapper.ConsentRequestMapperImpl;
import ch.agridata.agreement.persistence.ConsentRequestEntity;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.user.api.UserApi;
import ch.agridata.user.dto.UidDto;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsentRequestMutationServiceTest {

  static final String UID1 = "uid1";
  static final String UID2 = "uid2";
  static final String UID3 = "uid3";
  @Spy
  private final ConsentRequestMapper consentRequestMapper = new ConsentRequestMapperImpl();
  @Mock
  private ConsentRequestRepository consentRequestRepository;
  @Mock
  private AuditingService auditingService;
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
  private SessionFactory sessionFactory;
  @InjectMocks
  private ConsentRequestMutationService service;

  @Test
  void givenActiveDataRequestAndAuthorizedUids_whenCreateConsentRequestForDataRequest_thenCreateConsentRequestsTransactional() {
    // Given
    UUID dataRequestId = UUID.randomUUID();
    String ktIdP = "test-kt-id";

    List<String> authorizedUids = List.of(UID1, UID2, UID3);
    List<UidDto> uidDtos = authorizedUids.stream().map(uid -> UidDto.builder().uid(uid).build()).toList();

    DataRequestEntity dataRequest = DataRequestEntity.builder()
        .id(dataRequestId)
        .stateCode(DataRequestEntity.DataRequestStateEnum.ACTIVE)
        .build();

    // Mock existing consent requests (uid2 already has one)
    ConsentRequestEntity existingConsentRequest = ConsentRequestEntity.builder()
        .dataProducerUid(UID2)
        .build();
    List<ConsentRequestEntity> existingConsentRequests = List.of(existingConsentRequest);


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
    when(agridataSecurityIdentity.getKtIdpOfUserOrImpersonatedUser()).thenReturn(ktIdP);
    when(userApi.getAuthorizedUids(ktIdP)).thenReturn(uidDtos);
    when(consentRequestRepository.findByDataRequestIdAndDataProducerUids(dataRequestId, authorizedUids))
        .thenReturn(existingConsentRequests);
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
    // Mock the session factory transaction
    when(sessionFactory.fromTransaction(any())).thenAnswer(invocation -> {
      Function<Object, List<ConsentRequestCreatedDto>> transactionFunction = invocation.getArgument(0);
      return transactionFunction.apply(null);

    });

    // When
    List<ConsentRequestCreatedDto> result = service.createConsentRequestForDataRequest(dataRequestId);

    // Then
    assertThat(result)
        .hasSize(3)
        .containsExactlyInAnyOrder(dto1, dto2, dto3);


    // Verify that only the UIDs without existing consent requests were processed
    verify(consentRequestRepository).findByDataRequestIdAndDataProducerUids(dataRequestId, authorizedUids);
  }

  @Test
  void givenInactiveDataRequest_whenCreateConsentRequestForDataRequest_thenThrowIllegalStateException() {
    // Given
    UUID dataRequestId = UUID.randomUUID();
    DataRequestEntity inactiveDataRequest = DataRequestEntity.builder()
        .id(dataRequestId)
        .stateCode(DataRequestEntity.DataRequestStateEnum.DRAFT)
        .build();

    when(dataRequestRepository.findByIdOptional(dataRequestId)).thenReturn(Optional.of(inactiveDataRequest));

    // When & Then
    assertThatThrownBy(() -> service.createConsentRequestForDataRequest(dataRequestId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(dataRequestId.toString());
  }

  @Test
  void givenNonExistentDataRequest_whenCreateConsentRequestForDataRequest_thenThrowNotFoundException() {
    // Given
    UUID dataRequestId = UUID.randomUUID();
    when(dataRequestRepository.findByIdOptional(dataRequestId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> service.createConsentRequestForDataRequest(dataRequestId))
        .isInstanceOf(NotFoundException.class)
        .hasMessage(dataRequestId.toString());
  }

}
