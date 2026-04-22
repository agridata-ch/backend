package ch.agridata.datatransferv2.service;

import static ch.agridata.datatransferv2.service.client.DataProviderRestClientProvider.RestClientIdentifier.AGIS_API;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.api.ConsentRequestApi;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.datatransferv2.dto.ProducerIdentifier;
import ch.agridata.datatransferv2.service.client.DataProviderRestClient;
import ch.agridata.datatransferv2.service.client.DataProviderRestClientProvider;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import ch.agridata.product.service.DataProductService;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChangeDetectionServiceTest {

  private static final UUID PRODUCT_ID = UUID.randomUUID();
  private static final LocalDate LAST_MODIFIED_FROM = LocalDate.of(2026, 1, 1);
  private static final String CONSUMER_UID = "CHE123456789";
  private static final String CONSUMER_AGATE_LOGIN_ID = "20154600";

  @Mock
  DataProductService dataProductService;

  @Mock
  ConsentRequestApi consentRequestApi;

  @Mock
  DataProviderRestClientProvider dataProviderRestClientProvider;

  @Mock
  AgridataSecurityIdentity securityIdentity;

  @Mock
  DataProviderRestClient dataProviderRestClient;

  @Captor
  ArgumentCaptor<String> pathCaptor;

  @Captor
  ArgumentCaptor<DataProviderRestClient.Headers> headersCaptor;

  @InjectMocks
  ChangeDetectionService service;

  @Test
  void givenNullChangeDetectionPathTemplate_whenGetModifiedProducers_thenThrowsIllegalArgumentException() {
    var config = configBuilder().restClientChangeDetectionPathTemplate(null).build();
    when(dataProductService.getProviderConfigurationById(PRODUCT_ID)).thenReturn(config);

    assertThatThrownBy(() -> service.getModifiedProducers(PRODUCT_ID, LAST_MODIFIED_FROM))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Change detection is not supported for product=" + PRODUCT_ID);
  }

  @Test
  void givenBurBasedFlow_whenGetModifiedProducers_thenThrowsIllegalArgumentException() {
    var config = configBuilder()
        .flowCode("BUR_BASED_POST_VALIDATION")
        .restClientChangeDetectionPathTemplate("/changes?since={{LAST_CHANGED_SINCE_DATE_TIME}}")
        .build();
    when(dataProductService.getProviderConfigurationById(PRODUCT_ID)).thenReturn(config);

    assertThatThrownBy(() -> service.getModifiedProducers(PRODUCT_ID, LAST_MODIFIED_FROM))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Change detection is not supported for product=" + PRODUCT_ID);
  }

  @Test
  void givenUnboundFlow_whenGetModifiedProducers_thenThrowsIllegalArgumentException() {
    var config = configBuilder()
        .flowCode("UNBOUND_POST_VALIDATION")
        .restClientChangeDetectionPathTemplate("/changes?since={{LAST_CHANGED_SINCE_DATE_TIME}}")
        .build();
    when(dataProductService.getProviderConfigurationById(PRODUCT_ID)).thenReturn(config);

    assertThatThrownBy(() -> service.getModifiedProducers(PRODUCT_ID, LAST_MODIFIED_FROM))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Change detection is not supported for product=" + PRODUCT_ID);
  }

  @Test
  void givenNewConsentAndNoProviderChanges_whenGetModifiedProducers_thenReturnsNewlyConsentedUids() {
    var config = uidBasedConfig();
    when(dataProductService.getProviderConfigurationById(PRODUCT_ID)).thenReturn(config);
    when(consentRequestApi.getGrantedConsentRequestUidsForProductOfCurrentConsumerSince(eq(PRODUCT_ID), any(LocalDateTime.class)))
        .thenReturn(List.of("CHE111111111", "CHE222222222")) // all consents (epoch call)
        .thenReturn(List.of("CHE222222222")); // new consents
    mockProviderResponse(List.of());

    var result = service.getModifiedProducers(PRODUCT_ID, LAST_MODIFIED_FROM);

    assertThat(result).containsExactly(new ProducerIdentifier("CHE222222222", null));
  }

  @Test
  void givenProviderChangesWithConsent_whenGetModifiedProducers_thenReturnsChangedUids() {
    var config = uidBasedConfig();
    when(dataProductService.getProviderConfigurationById(PRODUCT_ID)).thenReturn(config);
    when(consentRequestApi.getGrantedConsentRequestUidsForProductOfCurrentConsumerSince(eq(PRODUCT_ID), any(LocalDateTime.class)))
        .thenReturn(List.of("CHE111111111", "CHE333333333")) // all consents
        .thenReturn(List.of()); // no new consents
    mockProviderResponse(List.of("CHE111111111", "CHE999999999")); // CHE999999999 has no consent

    var result = service.getModifiedProducers(PRODUCT_ID, LAST_MODIFIED_FROM);

    assertThat(result).containsExactly(new ProducerIdentifier("CHE111111111", null));
  }

  @Test
  void givenNewConsentAndProviderChanges_whenGetModifiedProducers_thenReturnsUnionWithoutDuplicates() {
    var config = uidBasedConfig();
    when(dataProductService.getProviderConfigurationById(PRODUCT_ID)).thenReturn(config);
    when(consentRequestApi.getGrantedConsentRequestUidsForProductOfCurrentConsumerSince(eq(PRODUCT_ID), any(LocalDateTime.class)))
        .thenReturn(List.of("CHE111111111", "CHE222222222")) // all consents
        .thenReturn(List.of("CHE222222222")); // new consents
    mockProviderResponse(List.of("CHE111111111", "CHE222222222")); // both changed

    var result = service.getModifiedProducers(PRODUCT_ID, LAST_MODIFIED_FROM);

    assertThat(result).hasSize(2);
    assertThat(result).extracting(ProducerIdentifier::uid).containsExactly("CHE111111111", "CHE222222222");
  }

  @Test
  void givenNoChanges_whenGetModifiedProducers_thenReturnsEmptyList() {
    var config = uidBasedConfig();
    when(dataProductService.getProviderConfigurationById(PRODUCT_ID)).thenReturn(config);
    when(consentRequestApi.getGrantedConsentRequestUidsForProductOfCurrentConsumerSince(eq(PRODUCT_ID), any(LocalDateTime.class)))
        .thenReturn(List.of("CHE111111111"))
        .thenReturn(List.of());
    mockProviderResponse(List.of());

    var result = service.getModifiedProducers(PRODUCT_ID, LAST_MODIFIED_FROM);

    assertThat(result).isEmpty();
  }

  @Test
  void givenUidBasedPostValidationFlow_whenGetModifiedProducers_thenReturnsResults() {
    var config = configBuilder()
        .flowCode("UID_BASED_POST_VALIDATION")
        .restClientChangeDetectionPathTemplate("/changes?since={{LAST_CHANGED_SINCE_DATE_TIME}}")
        .build();
    when(dataProductService.getProviderConfigurationById(PRODUCT_ID)).thenReturn(config);
    when(consentRequestApi.getGrantedConsentRequestUidsForProductOfCurrentConsumerSince(eq(PRODUCT_ID), any(LocalDateTime.class)))
        .thenReturn(List.of("CHE111111111"))
        .thenReturn(List.of("CHE111111111"));
    mockProviderResponse(List.of());

    var result = service.getModifiedProducers(PRODUCT_ID, LAST_MODIFIED_FROM);

    assertThat(result).containsExactly(new ProducerIdentifier("CHE111111111", null));
  }

  @Test
  void givenChangeDetectionPathTemplate_whenGetModifiedProducers_thenPathPlaceholderIsReplacedAndUrlEncoded() {
    var config = uidBasedConfig();
    when(dataProductService.getProviderConfigurationById(PRODUCT_ID)).thenReturn(config);
    when(consentRequestApi.getGrantedConsentRequestUidsForProductOfCurrentConsumerSince(any(), any()))
        .thenReturn(List.of())
        .thenReturn(List.of());
    when(securityIdentity.getUid()).thenReturn(Optional.of(CONSUMER_UID));
    when(securityIdentity.getAgateLoginId()).thenReturn(CONSUMER_AGATE_LOGIN_ID);
    when(dataProviderRestClientProvider.get(AGIS_API)).thenReturn(dataProviderRestClient);
    var mockResponse = mock(Response.class);
    when(dataProviderRestClient.get(pathCaptor.capture(), headersCaptor.capture())).thenReturn(mockResponse);
    doReturn(List.of()).when(mockResponse).readEntity(any(GenericType.class));

    service.getModifiedProducers(PRODUCT_ID, LAST_MODIFIED_FROM);

    assertThat(pathCaptor.getValue())
        .contains("2026-01-01T00%3A00%3A00")
        .doesNotContain("{{LAST_CHANGED_SINCE_DATE_TIME}}");
    assertThat(headersCaptor.getValue().agridataConsumerUid).isEqualTo(CONSUMER_UID);
    assertThat(headersCaptor.getValue().agridataConsumerAgateLoginId).isEqualTo(CONSUMER_AGATE_LOGIN_ID);
  }

  @Test
  void givenResultUids_whenGetModifiedProducers_thenResultIsSorted() {
    var config = uidBasedConfig();
    when(dataProductService.getProviderConfigurationById(PRODUCT_ID)).thenReturn(config);
    when(consentRequestApi.getGrantedConsentRequestUidsForProductOfCurrentConsumerSince(eq(PRODUCT_ID), any(LocalDateTime.class)))
        .thenReturn(List.of("CHE333333333", "CHE111111111", "CHE222222222"))
        .thenReturn(List.of("CHE333333333", "CHE111111111", "CHE222222222"));
    mockProviderResponse(List.of());

    var result = service.getModifiedProducers(PRODUCT_ID, LAST_MODIFIED_FROM);

    assertThat(result).extracting(ProducerIdentifier::uid)
        .isSorted();
  }

  private void mockProviderResponse(List<String> changedUids) {
    when(securityIdentity.getUid()).thenReturn(Optional.of(CONSUMER_UID));
    when(securityIdentity.getAgateLoginId()).thenReturn(CONSUMER_AGATE_LOGIN_ID);
    when(dataProviderRestClientProvider.get(AGIS_API)).thenReturn(dataProviderRestClient);
    var mockResponse = mock(Response.class);
    when(dataProviderRestClient.get(any(), any())).thenReturn(mockResponse);
    doReturn(changedUids).when(mockResponse).readEntity(any(GenericType.class));
  }

  private DataProductProviderConfigurationDto uidBasedConfig() {
    return configBuilder()
        .flowCode("UID_BASED_PRE_VALIDATION")
        .restClientChangeDetectionPathTemplate("/changes?since={{LAST_CHANGED_SINCE_DATE_TIME}}")
        .build();
  }

  private DataProductProviderConfigurationDto.DataProductProviderConfigurationDtoBuilder configBuilder() {
    return DataProductProviderConfigurationDto.builder()
        .id(PRODUCT_ID)
        .restClientIdentifierCode("AGIS_API")
        .restClientMethodCode("GET")
        .restClientPathTemplate("/farm-data/uid/{{uid}}");
  }
}
