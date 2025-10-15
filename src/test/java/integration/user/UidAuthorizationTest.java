package integration.user;

import static ch.agridata.user.dto.LegalFormEnum.AKTIENGESELLSCHAFT;
import static ch.agridata.user.dto.LegalFormEnum.EINFACHE_GESELLSCHAFT;
import static ch.agridata.user.dto.LegalFormEnum.NATUERLICHE_PERSON;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.PRODUCER_032;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.user.controller.UserController;
import ch.agridata.user.dto.UidDto;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@RequiredArgsConstructor
class UidAuthorizationTest {

  static Stream<Arguments> authorizedUidDataProvider() {
    return Stream.of(
        Arguments.of(
            "***032", List.of(
                new UidDto("CHE***435", "*** ***", NATUERLICHE_PERSON),
                new UidDto("CHE***860", "***", EINFACHE_GESELLSCHAFT))),
        Arguments.of(
            "***037", List.of(
                new UidDto("CHE***553", "*** ***", NATUERLICHE_PERSON),
                new UidDto("CHE***860", "***", EINFACHE_GESELLSCHAFT))),
        Arguments.of(
            "***081", List.of(
                new UidDto("CHE***142", "*** ***", NATUERLICHE_PERSON),
                new UidDto("CHE***280", "***", AKTIENGESELLSCHAFT),
                new UidDto("CHE***801", "***", AKTIENGESELLSCHAFT))),
        Arguments.of(
            "***266", List.of(
                new UidDto("CHE***278", "***", EINFACHE_GESELLSCHAFT),
                new UidDto("CHE***811", "***", EINFACHE_GESELLSCHAFT))),
        Arguments.of(
            "***307", List.of(
                new UidDto("CHE***744", "*** ***", NATUERLICHE_PERSON))),
        Arguments.of(
            "***401", List.of(
                new UidDto("CHE***299", "***", EINFACHE_GESELLSCHAFT),
                new UidDto("CHE***917", "***", EINFACHE_GESELLSCHAFT))),
        Arguments.of(
            "***451", List.of(
                new UidDto("CHE***632", "*** ***", NATUERLICHE_PERSON),
                new UidDto("CHE***917", "***", EINFACHE_GESELLSCHAFT),
                new UidDto("CHE***286", "***", EINFACHE_GESELLSCHAFT))),
        Arguments.of(
            "***479", List.of(
                new UidDto("CHE***948", "*** ***", NATUERLICHE_PERSON))),
        Arguments.of(
            "***724", List.of(
                new UidDto("CHE***505", "***", AKTIENGESELLSCHAFT))),
        Arguments.of(
            "***617", List.of(
                new UidDto("CHE***186", "*** ***", NATUERLICHE_PERSON))),
        Arguments.of(
            "***917", List.of(
                new UidDto("CHE***505", "***", AKTIENGESELLSCHAFT)))
    );
  }

  @ParameterizedTest
  @MethodSource("authorizedUidDataProvider")
  void testGetAuthorizedUidsByKtIdP(String ktIdP, List<UidDto> expectedResult) {
    var actualResult = AuthTestUtils.requestAs(ADMIN).accept(ContentType.JSON).when()
        .get(UserController.PATH + "/ktIdP/" + ktIdP + "/authorized-uids").then().statusCode(200)
        .extract()
        .as(new TypeRef<List<UidDto>>() {
        });

    assertThat(actualResult).usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expectedResult);
  }

  @Test
  void testGetAuthorizedUids() {
    var actualResult = AuthTestUtils.requestAs(PRODUCER_032).accept(ContentType.JSON).when()
        .get(UserController.PATH + "/authorized-uids").then().statusCode(200)
        .extract()
        .as(new TypeRef<List<UidDto>>() {
        });

    assertThat(actualResult).usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(
            List.of(
                new UidDto("CHE***435", "*** ***", NATUERLICHE_PERSON),
                new UidDto("CHE***860", "***", EINFACHE_GESELLSCHAFT)));
  }

}
