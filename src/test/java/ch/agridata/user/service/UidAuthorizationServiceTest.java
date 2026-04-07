package ch.agridata.user.service;

import static ch.agridata.user.dto.LegalFormEnum.EQUIDENEIGENTUEMER;
import static ch.agridata.user.dto.LegalFormEnum.NATUERLICHE_PERSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import ch.agridata.common.exceptions.UidMissingException;
import ch.agridata.common.exceptions.UidProviderUnavailableException;
import ch.agridata.user.dto.UidDto;
import java.util.List;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UidAuthorizationServiceTest {

  @Mock
  FarmerUidProvider farmerUidProvider;

  @Mock
  EquidOwnerUidProvider equidOwnerUidProvider;

  @Mock
  ManagedExecutor managedExecutor;

  @InjectMocks
  UidAuthorizationService uidAuthorizationService;

  @BeforeEach
  void setUp() {
    doAnswer(invocation -> {
      invocation.<Runnable>getArgument(0).run();
      return null;
    }).when(managedExecutor).execute(any(Runnable.class));
  }

  @Test
  void givenUidFromAnyProvider_whenGetAuthorizedUids_thenReturnMergedDistinctUids() {
    when(farmerUidProvider.getAuthorizedUids("KT123")).thenReturn(List.of(
        new UidDto("CHE123", "Farmer Person", NATUERLICHE_PERSON),
        new UidDto("CHE999", "Shared UID", NATUERLICHE_PERSON)
    ));
    when(equidOwnerUidProvider.getAuthorizedUids("AGATE123")).thenReturn(List.of(
        new UidDto("CHE999", "Shared UID from TVD", EQUIDENEIGENTUEMER),
        new UidDto("CHE456", "Equid Owner", EQUIDENEIGENTUEMER)
    ));

    var result = uidAuthorizationService.getAuthorizedUids("KT123", "AGATE123");

    assertThat(result).containsExactlyInAnyOrder(
        new UidDto("CHE123", "Farmer Person", NATUERLICHE_PERSON),
        new UidDto("CHE999", "Shared UID", NATUERLICHE_PERSON),
        new UidDto("CHE456", "Equid Owner", EQUIDENEIGENTUEMER)
    );
  }

  @Test
  void givenNoUidAndBothProvidersAvailable_whenGetAuthorizedUids_thenThrowUidMissingException() {
    when(farmerUidProvider.getAuthorizedUids("KT123")).thenReturn(List.of());
    when(equidOwnerUidProvider.getAuthorizedUids("AGATE123")).thenReturn(List.of());

    assertThatThrownBy(() -> uidAuthorizationService.getAuthorizedUids("KT123", "AGATE123"))
        .isInstanceOf(UidMissingException.class)
        .hasMessageContaining("synchronization may still be in progress");
  }

  @Test
  void givenNoUidAndFarmerProviderUnavailable_whenGetAuthorizedUids_thenThrowUidProviderUnavailableException() {
    when(farmerUidProvider.getAuthorizedUids("KT123"))
        .thenThrow(new UidProviderUnavailableException("AGIS unavailable"));
    when(equidOwnerUidProvider.getAuthorizedUids("AGATE123")).thenReturn(List.of());

    assertThatThrownBy(() -> uidAuthorizationService.getAuthorizedUids("KT123", "AGATE123"))
        .isInstanceOf(UidProviderUnavailableException.class)
        .hasMessageContaining("one or more external services are unavailable");
  }

  @Test
  void givenNoUidAndEquidOwnerProviderUnavailable_whenGetAuthorizedUids_thenThrowUidProviderUnavailableException() {
    when(farmerUidProvider.getAuthorizedUids("KT123")).thenReturn(List.of());
    when(equidOwnerUidProvider.getAuthorizedUids("AGATE123"))
        .thenThrow(new UidProviderUnavailableException("TVD unavailable"));

    assertThatThrownBy(() -> uidAuthorizationService.getAuthorizedUids("KT123", "AGATE123"))
        .isInstanceOf(UidProviderUnavailableException.class)
        .hasMessageContaining("one or more external services are unavailable");
  }
}
