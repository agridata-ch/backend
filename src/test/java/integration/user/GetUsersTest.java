package integration.user;

import static integration.testutils.TestUserEnum.PRODUCER_032;
import static integration.testutils.TestUserEnum.SUPPORT;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.controller.ConsentRequestController;
import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.user.controller.UserController;
import ch.agridata.user.dto.UserInfoDto;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class GetUsersTest {
  private final Flyway flyway;
  private static final int TOTAL_PRODUCERS = 54;

  @BeforeEach
  void setUp() {
    // will make sure testdata prior to executing each test
    flyway.migrate();
  }

  @Test
  void givenProducers_getProducers_returnsFirstProducerPage() {
    var pageSize = 10;
    var page = 0;

    var actualResult = AuthTestUtils.requestAs(SUPPORT)
        .queryParam("size", pageSize)
        .queryParam("page", page)
        .when()
        .get(UserController.PATH + "/producers").then().statusCode(200)
        .extract()
        .as(new TypeRef<PageResponseDto<UserInfoDto>>() {
        });
    assertThat(actualResult.items()).hasSize(pageSize);
    assertThat(actualResult.currentPage()).isZero();
    assertThat(actualResult.totalItems()).isEqualTo(TOTAL_PRODUCERS);
  }

  @Test
  void givenProducers_getProducers_returnsLastProducerPage() {
    var pageSize = 10;
    var lastPage = Math.floorDiv(TOTAL_PRODUCERS, pageSize);
    var expectedRemainingElements = Math.floorMod(TOTAL_PRODUCERS, pageSize);

    var actualResult = AuthTestUtils.requestAs(SUPPORT)
        .queryParam("size", pageSize)
        .queryParam("page", lastPage)
        .when()
        .get(UserController.PATH + "/producers").then().statusCode(200)
        .extract()
        .as(new TypeRef<PageResponseDto<UserInfoDto>>() {
        });
    assertThat(actualResult.items()).hasSize(expectedRemainingElements);
    assertThat(actualResult.currentPage()).isEqualTo(lastPage);
  }

  @Test
  void givenProducerWithNameJohnSmith_getProducers_returnsProducer() {
    var actualResult = AuthTestUtils.requestAs(SUPPORT)
        .given()
        .queryParams("searchTerm", "sm Jo")
        .when()
        .get(UserController.PATH + "/producers").then().statusCode(200)
        .extract()
        .as(new TypeRef<PageResponseDto<UserInfoDto>>() {
        });
    assertThat(actualResult.items()).hasSize(1);
  }

  @Test
  void givenRecentlyCreatedProducer_getProducers_returnsProducerWithAllUserData() {
    // Trigger any authenticated request as a producer to ensure the user record is created and stored
    AuthTestUtils.requestAs(PRODUCER_032)
        .header("Content-Type", MediaType.APPLICATION_JSON)
        .when()
        .get(ConsentRequestController.PATH)
        .then()
        .statusCode(200);

    var actualResult = AuthTestUtils.requestAs(SUPPORT)
        .queryParam("size", 10)
        .queryParam("page", 0)
        .queryParams("sortBy", "-lastLoginDate")
        .when()
        .get(UserController.PATH + "/producers").then().statusCode(200)
        .extract()
        .as(new TypeRef<PageResponseDto<UserInfoDto>>() {
        });

    var producer = actualResult.items().getFirst();

    assertThat(producer.ktIdP()).isEqualTo(PRODUCER_032.getKtIdP());
    assertThat(producer.lastLoginDate()).isBetween(LocalDateTime.now().minusSeconds(5), LocalDateTime.now().plusSeconds(5));
    assertThat(
        List.of(
            producer.addressCountry(),
            producer.addressLocality(),
            producer.addressPostalCode(),
            producer.addressStreet(),
            producer.email(),
            producer.familyName(),
            producer.givenName(),
            producer.phoneNumber()
        ))
        .isNotEmpty()
        .allSatisfy(field -> assertThat(field).isNotBlank());
  }
}
