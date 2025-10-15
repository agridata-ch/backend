package integration.user;

import static integration.testutils.TestUserEnum.SUPPORT;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.user.controller.UserController;
import ch.agridata.user.dto.UserInfoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
public class GetUsersTest {
  private final Flyway flyway;
  private final ObjectMapper mapper;
  private static final int TOTAL_PRODUCERS = 54;

  @BeforeEach
  void setUp() {
    // will make sure testdata prior to executing each test
    flyway.migrate();
  }

  @Test
  public void givenProducers_getProducers_returnsFirstProducerPage() {
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
  public void givenProducers_getProducers_returnsLastProducerPage() {
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
  public void givenProducerWithNameJohnSmith_getProducers_returnsProducer() {
    var query = ResourceQueryDto.builder().searchTerm("sm Jo").build();

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

}
