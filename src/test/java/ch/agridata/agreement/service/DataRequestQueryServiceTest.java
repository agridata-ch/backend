package ch.agridata.agreement.service;

import static ch.agridata.agreement.utils.DataRequestTestUtils.USER_UID;
import static ch.agridata.agreement.utils.DataRequestTestUtils.buildEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.mapper.DataRequestMapper;
import ch.agridata.agreement.mapper.DataRequestMapperImpl;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.api.DataProductApi;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
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
  void givenRequestsExist_whenGetAllRequests_OfAdmin_thenReturnDtos() {
    var dataConsumer = buildEntity();
    PanacheQuery<DataRequestEntity> panacheMock = mock(PanacheQuery.class);
    when(repository.findAll()).thenReturn(panacheMock);
    when(panacheMock.list()).thenReturn(List.of(dataConsumer));

    var result = service.getAllDataRequests();

    assertEquals(1, result.size());
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
