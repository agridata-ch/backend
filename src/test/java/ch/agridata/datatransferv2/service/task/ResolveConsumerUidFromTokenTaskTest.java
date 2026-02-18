package ch.agridata.datatransferv2.service.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import ch.agridata.common.exceptions.UidMissingException;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.datatransferv2.service.FlowEnum;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResolveConsumerUidFromTokenTaskTest {

  private static final String CONSUMER_UID = "CHE123456789";
  private static final String AGATE_LOGIN_ID = "12345678";

  @Mock
  AgridataSecurityIdentity agridataSecurityIdentity;

  @InjectMocks
  ResolveConsumerUidFromTokenTask task;

  @Test
  void givenValidToken_whenApply_thenConsumerUidIsResolved() {
    var context = createContext();
    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(CONSUMER_UID);
    when(agridataSecurityIdentity.getAgateLoginId()).thenReturn(AGATE_LOGIN_ID);

    var result = task.apply(context);

    assertThat(result.getConsumerUid()).isEqualTo(CONSUMER_UID);
    assertThat(result.getConsumerAgateLoginId()).isEqualTo(AGATE_LOGIN_ID);
  }

  @Test
  void givenTokenWithoutUid_whenApply_thenUidMissingExceptionIsThrown() {
    var context = createContext();
    when(agridataSecurityIdentity.getUidOrElseThrow())
        .thenThrow(new UidMissingException("User has no UID assigned"));

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(UidMissingException.class)
        .hasMessageContaining("no UID assigned");
  }

  private AgridataContext createContext() {
    return AgridataContext.builder()
        .productId(UUID.randomUUID())
        .flowEnum(FlowEnum.UID_BASED_PRE_VALIDATION)
        .build();
  }
}
