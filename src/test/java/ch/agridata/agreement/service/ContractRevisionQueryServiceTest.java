package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.dto.ContractRevisionDto;
import ch.agridata.agreement.mapper.ContractRevisionMapper;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.agreement.persistence.ContractRevisionRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.ws.rs.NotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractRevisionQueryServiceTest {
  @Mock
  private ContractRevisionRepository contractRevisionRepository;

  @Mock
  private ContractRevisionMapper contractRevisionMapper;

  @Mock
  private AgridataSecurityIdentity agridataSecurityIdentity;

  @InjectMocks
  ContractRevisionQueryService service;

  @Test
  void givenExistingContractRevisionForCurrentConsumer_whenGetDataRequestForCurrentConsumer_thenReturnMappedDto() {
    UUID contractRevisionId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    String consumerUid = "CHE000000001";

    ContractRevisionEntity entity = ContractRevisionEntity.builder().id(contractRevisionId).build();
    ContractRevisionDto expectedDto = ContractRevisionDto.builder().id(contractRevisionId).build();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(consumerUid);
    when(contractRevisionRepository.findByIdAndDataConsumerUid(contractRevisionId, consumerUid))
        .thenReturn(Optional.of(entity));
    when(contractRevisionMapper.toDto(entity)).thenReturn(expectedDto);

    ContractRevisionDto result = service.getContractRevisionOfCurrentConsumer(contractRevisionId);

    assertThat(result).isSameAs(expectedDto);
    verify(contractRevisionRepository).findByIdAndDataConsumerUid(contractRevisionId, consumerUid);
    verify(contractRevisionMapper).toDto(entity);
  }

  @Test
  void givenMissingContractRevisionForCurrentConsumer_whenGetContractRevisionOfCurrentConsumer_thenThrowNotFoundException() {
    UUID contractRevisionId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    String consumerUid = "CHE000000001";

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(consumerUid);
    when(contractRevisionRepository.findByIdAndDataConsumerUid(contractRevisionId, consumerUid))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getContractRevisionOfCurrentConsumer(contractRevisionId))
        .isInstanceOf(NotFoundException.class)
        .hasMessage(contractRevisionId.toString());

    verify(contractRevisionRepository)
        .findByIdAndDataConsumerUid(contractRevisionId, consumerUid);
    verifyNoInteractions(contractRevisionMapper);
  }
}
