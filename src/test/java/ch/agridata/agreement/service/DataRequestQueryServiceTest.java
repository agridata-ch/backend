package ch.agridata.agreement.service;

import static ch.agridata.agreement.utils.DataRequestTestUtils.USER_UID;
import static ch.agridata.agreement.utils.DataRequestTestUtils.buildEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.mapper.DataRequestMapper;
import ch.agridata.agreement.mapper.DataRequestMapperImpl;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.api.DataProductApi;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataRequestQueryServiceTest {

  @Spy
  private final DataRequestMapper mapper = new DataRequestMapperImpl();
  @InjectMocks
  private DataRequestQueryService service;
  @Mock
  private DataRequestRepository repository;
  @Mock
  private DataProductApi dataProductApi;
  @Mock
  private AgridataSecurityIdentity agridataSecurityIdentity;

  @Test
  void givenRequestsExist_whenGetAllRequests_OfConsumer_thenReturnDtos() {
    var dataConsumer = buildEntity();
    when(repository.findByDataConsumerUid(USER_UID)).thenReturn(List.of(dataConsumer));
    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);

    var result = service.getAllDataRequestsOfCurrentConsumer();

    assertEquals(1, result.size());
  }

  @Test
  void givenRequestsExist_whenGetAllNonDraftRequests_OfAdmin_thenReturnDtos() {
    var entity = buildEntity();
    when(repository.findAllNotDraft()).thenReturn(List.of(entity));

    var result = service.getAllNonDraftDataRequests();

    assertEquals(1, result.size());
  }

  @Test
  void givenNonDraftRequestExists_whenGetNonDraftRequests_OfAdmin_thenReturnDto() {
    var id = UUID.randomUUID();
    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.IN_REVIEW);

    when(repository.findByIdAndStateCodeNotDraft(id)).thenReturn(Optional.of(entity));

    var result = service.getNonDraftDataRequest(id);

    assertThat(result).isNotNull();
  }

  @Test
  void givenDraftRequestExists_whenGetNonDraftRequests_OfAdmin_thenThrowNotFound() {
    var id = UUID.randomUUID();

    when(repository.findByIdAndStateCodeNotDraft(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.getNonDraftDataRequest(id));
  }

  @Test
  void givenValidId_whenGetDataRequestOfCurrentConsumer_thenReturnDto() {
    var id = UUID.randomUUID();
    var entity = buildEntity();

    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));
    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);

    var result = service.getDataRequestOfCurrentConsumer(id);

    assertThat(result).isNotNull();
  }

  @Test
  void givenInvalidId_whenGetDataRequestOfCurrentConsumer_thenThrowNotFound() {
    UUID id = UUID.randomUUID();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.getDataRequestOfCurrentConsumer(id));
  }
}
