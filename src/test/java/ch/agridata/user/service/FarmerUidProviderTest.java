package ch.agridata.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.agridata.agis.api.AgisApi;
import ch.agridata.agis.dto.AgisPersonFarmResponseType;
import ch.agridata.agis.dto.AgisPersonFarmTreeType;
import ch.agridata.agis.dto.AgisPersonType;
import ch.agridata.agis.dto.AgisRelevantPersons;
import ch.agridata.common.exceptions.UidProviderUnavailableException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FarmerUidProviderTest {

  @Mock
  AgisApi agisApi;

  @InjectMocks
  FarmerUidProvider farmerUidProvider;

  @Test
  void givenResponseWithoutPersonFarmTree_whenGetAuthorizedUids_thenReturnEmptyList() {
    when(agisApi.fetchRegisterDataForKtIdP("FLXXE0002")).thenReturn(new AgisPersonFarmResponseType());

    var result = farmerUidProvider.getAuthorizedUids("FLXXE0002");

    assertThat(result).isEmpty();
  }

  @Test
  void givenResponseWithoutRelevantPerson_whenGetAuthorizedUids_thenReturnEmptyList() {
    var response = mock(AgisPersonFarmResponseType.class);
    var personFarmTree = mock(AgisPersonFarmTreeType.class);
    var relevantPersons = mock(AgisRelevantPersons.class);

    when(agisApi.fetchRegisterDataForKtIdP("FLXXE0003")).thenReturn(response);
    when(response.getPersonFarmTree()).thenReturn(personFarmTree);
    when(personFarmTree.getRelevantPersons()).thenReturn(relevantPersons);
    when(relevantPersons.getPerson()).thenReturn(List.of());

    var result = farmerUidProvider.getAuthorizedUids("FLXXE0003");

    assertThat(result).isEmpty();
  }

  @Test
  void givenResponseWithRelevantPersonButWithoutMatchingFarm_whenGetAuthorizedUids_thenReturnEmptyList() {
    var response = mock(AgisPersonFarmResponseType.class);
    var personFarmTree = mock(AgisPersonFarmTreeType.class);
    var relevantPersons = mock(AgisRelevantPersons.class);
    var person = mock(AgisPersonType.class);

    when(agisApi.fetchRegisterDataForKtIdP("FLXXE0005")).thenReturn(response);
    when(response.getPersonFarmTree()).thenReturn(personFarmTree);
    when(personFarmTree.getRelevantPersons()).thenReturn(relevantPersons);
    when(relevantPersons.getPerson()).thenReturn(List.of(person));
    when(person.getKtIdP()).thenReturn("FLXXE0005");

    var result = farmerUidProvider.getAuthorizedUids("FLXXE0005");

    assertThat(result).isEmpty();
  }

  @Test
  void givenFallbackCalled_whenFallbackAuthorizedUids_thenThrowUidProviderUnavailableException() throws Exception {
    var rootCause = new RuntimeException("timeout");
    Method fallbackMethod = FarmerUidProvider.class.getDeclaredMethod("fallbackAuthorizedUids", String.class, Throwable.class);
    fallbackMethod.setAccessible(true);

    assertThatThrownBy(() -> fallbackMethod.invoke(farmerUidProvider, "FLXXE0004", rootCause))
        .isInstanceOf(InvocationTargetException.class)
        .satisfies(ex -> {
          var cause = ((InvocationTargetException) ex).getTargetException();
          assertThat(cause).isInstanceOf(UidProviderUnavailableException.class);
          assertThat(cause.getCause()).isSameAs(rootCause);
        });
  }

  @Test
  void givenNullKtIdPLogin_whenGetAuthorizedUids_thenThrowNullPointerException() {
    assertThatThrownBy(() -> farmerUidProvider.getAuthorizedUids(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void givenNullKtIdPLogin_whenFallbackAuthorizedUids_thenThrowNullPointerException() throws Exception {
    Method fallbackMethod = FarmerUidProvider.class.getDeclaredMethod("fallbackAuthorizedUids", String.class, Throwable.class);
    fallbackMethod.setAccessible(true);

    assertThatThrownBy(() -> fallbackMethod.invoke(farmerUidProvider, null, new RuntimeException("timeout")))
        .isInstanceOf(InvocationTargetException.class)
        .satisfies(ex -> assertThat(((InvocationTargetException) ex).getTargetException()).isInstanceOf(NullPointerException.class));
  }
}
