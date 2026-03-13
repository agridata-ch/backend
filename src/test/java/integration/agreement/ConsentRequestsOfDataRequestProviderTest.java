package integration.agreement;

import static integration.testutils.TestUserEnum.PROVIDER;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.controller.DataRequestController;
import ch.agridata.agreement.dto.ConsentRequestFundamentalViewDto;
import ch.agridata.agreement.mapper.ConsentRequestMapper;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import ch.agridata.common.dto.PageResponseDto;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import integration.testutils.TestDataIdentifiers.ConsentRequest;
import integration.testutils.TestDataLoader;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ConsentRequestsOfDataRequestProviderTest {

  private final Flyway flyway;
  private final ConsentRequestRepository consentRequestRepository;
  private final ConsentRequestMapper consentRequestMapper;

  @BeforeEach
  void setUp() {
    flyway.migrate();
  }

  @Test
  void givenProvider_whenRequestingConsentRequestsOfOwnDataRequest_thenAllConsentRequestsReturned() {
    // IP_SUISSE_01 belongs to the BLW/AGIS provider (uid=CHE146680598), which is the PROVIDER test user.
    // It has 4 consent requests.
    PageResponseDto<ConsentRequestFundamentalViewDto> response = AuthTestUtils.requestAs(PROVIDER)
        .when().get(DataRequestController.PATH_V1 + "/" + TestDataIdentifiers.DataRequest.IP_SUISSE_01 + "/consent-requests")
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    var expectedItems = TestDataLoader.of(consentRequestRepository).load(
            ConsentRequest.IP_SUISSE_01_CHE101000001,
            ConsentRequest.IP_SUISSE_01_CHE102000002,
            ConsentRequest.IP_SUISSE_01_CHE103000001,
            ConsentRequest.IP_SUISSE_01_CHE103000002).stream()
        .map(consentRequestMapper::toConsentRequestFundamentalViewDto)
        .toList();

    assertThat(response.items()).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expectedItems);
    assertThat(response.totalItems()).isEqualTo(4);
    assertThat(response.currentPage()).isZero();
  }

  @Test
  void givenProvider_whenRequestingWithPagination_thenCorrectPageAndMetadataReturned() {
    // IP_SUISSE_01 has 4 consent requests. With page=0 and size=2 we expect a partial page.
    PageResponseDto<ConsentRequestFundamentalViewDto> response = AuthTestUtils.requestAs(PROVIDER)
        .queryParam("page", 0)
        .queryParam("size", 2)
        .when().get(DataRequestController.PATH_V1 + "/" + TestDataIdentifiers.DataRequest.IP_SUISSE_01 + "/consent-requests")
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    assertThat(response.items()).hasSize(2);
    assertThat(response.totalItems()).isEqualTo(4);
    assertThat(response.totalPages()).isEqualTo(2);
    assertThat(response.currentPage()).isZero();
    assertThat(response.pageSize()).isEqualTo(2);
  }

  @Test
  void givenProvider_whenFilteringByLastModifiedFromFarInFuture_thenEmptyResultReturned() {
    // All test data has modifiedAt = NOW(), so a far-future filter must yield an empty result.
    PageResponseDto<ConsentRequestFundamentalViewDto> response = AuthTestUtils.requestAs(PROVIDER)
        .queryParam("lastModifiedFrom", "9999-12-31T00:00:00")
        .when().get(DataRequestController.PATH_V1 + "/" + TestDataIdentifiers.DataRequest.IP_SUISSE_01 + "/consent-requests")
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    assertThat(response.items()).isEmpty();
    assertThat(response.totalItems()).isZero();
  }

  @Test
  void givenProvider_whenRequestingDataRequestOfDifferentProvider_thenNotFound() {
    // BLV_1 uses the TVD/IDENTITAS data source system (uid=CHE105031830).
    // The PROVIDER test user is BLW (uid=CHE146680598) and must not see it.
    AuthTestUtils.requestAs(PROVIDER)
        .when().get(DataRequestController.PATH_V1 + "/" + TestDataIdentifiers.DataRequest.BLV_1 + "/consent-requests")
        .then().statusCode(404);
  }
}
