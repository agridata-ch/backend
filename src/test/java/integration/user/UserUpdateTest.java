package integration.user;

import static ch.agridata.common.filters.ImpersonationHeaderFilter.IMPERSONATION_HEADER;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.PRODUCER_032;
import static integration.testutils.TestUserEnum.SUPPORT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import ch.agridata.user.controller.UserController;
import ch.agridata.user.dto.UserInfoDto;
import ch.agridata.user.dto.UserPreferencesDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class UserUpdateTest {

  private final ObjectMapper mapper;

  @Test
  void givenAdminWithoutSomeAttributesSet_testUpdateUserData_isSuccessful() {
    var actualResult = AuthTestUtils.requestAs(ADMIN).when()
        .get(UserController.PATH + "/user-info").then().statusCode(200)
        .extract()
        .as(new TypeRef<UserInfoDto>() {
        });

    assertThat(actualResult)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFields("lastLoginDate")
        .isEqualTo(UserInfoDto.builder()
            .ktIdP(null)
            .uid(null)
            .givenName("Tom")
            .familyName("Admin")
            .email("tom.admin@blw.admin.ch")
            .phoneNumber(null)
            .addressStreet(null)
            .addressLocality(null)
            .addressPostalCode(null)
            .addressCountry(null)
            .build());

    assertThat(actualResult.lastLoginDate())
        .isAfter(LocalDateTime.now().minusMinutes(2))
        .isBefore(LocalDateTime.now().plusMinutes(1));
  }

  @Test
  void givenConsumerWithoutSomeAttributesSet_testUpdateUserData_isSuccessful() {
    var actualResult = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).when()
        .get(UserController.PATH + "/user-info").then().statusCode(200)
        .extract()
        .as(new TypeRef<UserInfoDto>() {
        });

    assertThat(actualResult)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFields("lastLoginDate")
        .isEqualTo(UserInfoDto.builder()
            .ktIdP(null)
            .uid("CHE101708094")
            .givenName("Lea")
            .familyName("Consumer")
            .email("lea.consumer@blw.admin.ch")
            .phoneNumber("+4179123456789")
            .addressStreet("Testfallgasse 9")
            .addressLocality("Thun")
            .addressPostalCode("3600")
            .addressCountry("CH")
            .build());

    assertThat(actualResult.lastLoginDate())
        .isAfter(LocalDateTime.now().minusMinutes(2))
        .isBefore(LocalDateTime.now().plusMinutes(1));
  }

  @Test
  void givenImpersonationHeader_whenGetUserInfo_thenReturnUserInfoOfImpersonatedUser() {
    // make sure the producer exists
    AuthTestUtils.requestAs(PRODUCER_032)
        .when()
        .get(UserController.PATH + "/user-info")
        .then()
        .statusCode(200);

    AuthTestUtils.requestAs(SUPPORT)
        .when()
        .header(IMPERSONATION_HEADER, PRODUCER_032.getKtIdP())
        .get(UserController.PATH + "/user-info")
        .then()
        .statusCode(200)
        .body("ktIdP", not(emptyOrNullString()))
        .body("ktIdP", equalTo(PRODUCER_032.getKtIdP()));

  }

  @Test
  void givenValidPreferences_whenUpdateUserPreferences_thenReturnCreated()
      throws com.fasterxml.jackson.core.JsonProcessingException {
    var preferences =
        UserPreferencesDto.builder().activeUid("123").mainMenuOpened(true).dismissedMigratedIds(List.of("mig-1", "mig-2")).build();


    AuthTestUtils.requestAs(PRODUCER_032)
        .when()
        .contentType(ContentType.JSON)
        .body(mapper.writeValueAsString(preferences))
        .put(UserController.PATH + "/preferences")
        .then()
        .statusCode(RestResponse.StatusCode.CREATED);

    UserInfoDto userInfo = AuthTestUtils.requestAs(PRODUCER_032)
        .when()
        .get(UserController.PATH + "/user-info")
        .then()
        .statusCode(200).extract().as(UserInfoDto.class);

    assertThat(userInfo.userPreferences()).usingRecursiveComparison().isEqualTo(preferences);
  }
}
