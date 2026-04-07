package ch.agridata.user.service;

import static ch.agridata.user.dto.LegalFormEnum.EQUIDENEIGENTUEMER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.common.exceptions.UidProviderUnavailableException;
import ch.agridata.tvd.api.TvdApi;
import ch.agridata.tvd.dto.TvdEquidOwnerUidDto;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EquidOwnerUidProviderTest {

  @Mock
  TvdApi tvdApi;

  @InjectMocks
  EquidOwnerUidProvider equidOwnerUidProvider;

  @Test
  void givenFeatureFlagDisabled_whenGetAuthorizedUids_thenReturnEmptyList() {
    equidOwnerUidProvider.featureEnabled = false;

    var result = equidOwnerUidProvider.getAuthorizedUids("3477589");

    assertThat(result).isEmpty();
    verifyNoInteractions(tvdApi);
  }

  @Test
  void givenFeatureFlagEnabled_whenGetAuthorizedUids_thenMapResult() {
    equidOwnerUidProvider.featureEnabled = true;
    when(tvdApi.fetchEquidOwnerLegalUnits("3477589")).thenReturn(List.of(
        TvdEquidOwnerUidDto.Data.builder()
            .uid("CHE123456789")
            .firstName("Test")
            .lastName("Owner")
            .build()));

    var result = equidOwnerUidProvider.getAuthorizedUids("3477589");

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().uid()).isEqualTo("CHE123456789");
    assertThat(result.getFirst().name()).isEqualTo("Test Owner");
    assertThat(result.getFirst().legalFormCode()).isEqualTo(EQUIDENEIGENTUEMER);
  }

  @Test
  void givenFeatureFlagEnabledAndNoLegalUnits_whenGetAuthorizedUids_thenReturnEmptyList() {
    equidOwnerUidProvider.featureEnabled = true;
    when(tvdApi.fetchEquidOwnerLegalUnits("3477589")).thenReturn(List.of());

    var result = equidOwnerUidProvider.getAuthorizedUids("3477589");

    assertThat(result).isEmpty();
  }

  @Test
  void givenNullAgateLoginId_whenGetAuthorizedUids_thenThrowNullPointerException() {
    equidOwnerUidProvider.featureEnabled = true;

    assertThatThrownBy(() -> equidOwnerUidProvider.getAuthorizedUids(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void givenNullAgateLoginId_whenFallbackAuthorizedUids_thenThrowNullPointerException() throws Exception {
    Method fallbackMethod = EquidOwnerUidProvider.class.getDeclaredMethod("fallbackAuthorizedUids", String.class, Throwable.class);
    fallbackMethod.setAccessible(true);

    assertThatThrownBy(() -> fallbackMethod.invoke(equidOwnerUidProvider, null, new RuntimeException("timeout")))
        .isInstanceOf(InvocationTargetException.class)
        .satisfies(ex -> assertThat(((InvocationTargetException) ex).getTargetException()).isInstanceOf(NullPointerException.class));
  }

  @Test
  void givenFallbackCalled_whenFallbackAuthorizedUids_thenThrowUidProviderUnavailableException() throws Exception {
    var rootCause = new RuntimeException("timeout");
    Method fallbackMethod = EquidOwnerUidProvider.class.getDeclaredMethod("fallbackAuthorizedUids", String.class, Throwable.class);
    fallbackMethod.setAccessible(true);

    assertThatThrownBy(() -> fallbackMethod.invoke(equidOwnerUidProvider, "3477589", rootCause))
        .isInstanceOf(InvocationTargetException.class)
        .satisfies(ex -> {
          var cause = ((InvocationTargetException) ex).getTargetException();
          assertThat(cause).isInstanceOf(UidProviderUnavailableException.class);
          assertThat(cause.getCause()).isSameAs(rootCause);
        });
  }
}
