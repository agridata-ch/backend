package integration.user;

import static ch.agridata.user.dto.FarmTypeEnum.BETRIEBSGEMEINSCHAFT;
import static ch.agridata.user.dto.FarmTypeEnum.BETRIEBSZWEIGGEMEINSCHAFT;
import static ch.agridata.user.dto.FarmTypeEnum.GANZJAHRESBETRIEB;
import static ch.agridata.user.dto.FarmTypeEnum.OELN_GEMEINSCHAFT;
import static ch.agridata.user.dto.FarmTypeEnum.PRODUKTIONSSTAETTE;
import static ch.agridata.user.dto.FarmTypeEnum.SOEMMERUNGSBETRIEB;
import static ch.agridata.user.dto.FarmTypeEnum.TIERHALTUNG;
import static ch.agridata.user.dto.FarmTypeEnum.VIEHHANDELSUNTERNEHMEN;
import static ch.agridata.user.dto.FarmTypeEnum.VIEHMAERKTE_UND_VERANSTALTUNGEN;
import static integration.testutils.TestUserEnum.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.user.controller.UserController;
import ch.agridata.user.dto.BurDto;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@RequiredArgsConstructor
class BurAuthorizationTest {

  static Stream<Arguments> authorizedBurDataProvider() {
    return Stream.of(
        Arguments.of(
            "CHE***278", List.of(
                new BurDto("***629", GANZJAHRESBETRIEB),
                new BurDto("***440", PRODUKTIONSSTAETTE),
                new BurDto("***414", PRODUKTIONSSTAETTE),
                new BurDto("***519", SOEMMERUNGSBETRIEB))),
        Arguments.of(
            "CHE***744", List.of(
                new BurDto("***693", GANZJAHRESBETRIEB),
                new BurDto("***962", SOEMMERUNGSBETRIEB))),
        Arguments.of(
            "CHE***948", List.of(
                new BurDto("***379", GANZJAHRESBETRIEB),
                new BurDto("***303", PRODUKTIONSSTAETTE),
                new BurDto("***298", PRODUKTIONSSTAETTE))),
        Arguments.of(
            "CHE***505", List.of(
                new BurDto("***482", GANZJAHRESBETRIEB),
                new BurDto("***791", VIEHHANDELSUNTERNEHMEN),
                new BurDto("***357", PRODUKTIONSSTAETTE),
                new BurDto("***315", VIEHHANDELSUNTERNEHMEN),
                new BurDto("***615", VIEHHANDELSUNTERNEHMEN),
                new BurDto("***036", VIEHMAERKTE_UND_VERANSTALTUNGEN),
                new BurDto("***819", PRODUKTIONSSTAETTE),
                new BurDto("***782", PRODUKTIONSSTAETTE),
                new BurDto("***638", TIERHALTUNG),
                new BurDto("***270", VIEHMAERKTE_UND_VERANSTALTUNGEN))),
        Arguments.of(
            "CHE***435", List.of(
                new BurDto("***566", GANZJAHRESBETRIEB))),
        Arguments.of(
            "CHE***860", List.of(
                new BurDto("***784", BETRIEBSGEMEINSCHAFT))),
        Arguments.of(
            "CHE***553", List.of(
                new BurDto("***917", GANZJAHRESBETRIEB))),
        Arguments.of(
            "CHE***142", List.of(
                new BurDto("***808", GANZJAHRESBETRIEB))),
        Arguments.of(
            "CHE***280", List.of(
                new BurDto("***894", GANZJAHRESBETRIEB))),
        Arguments.of(
            "CHE***801", List.of(
                new BurDto("***332", GANZJAHRESBETRIEB),
                new BurDto("***011", TIERHALTUNG))),
        Arguments.of(
            "CHE***299", List.of(
                new BurDto("***506", GANZJAHRESBETRIEB))),
        Arguments.of(
            "CHE***632", List.of(
                new BurDto("***066", GANZJAHRESBETRIEB))),
        Arguments.of(
            "CHE***917", List.of(
                new BurDto("***004", OELN_GEMEINSCHAFT))),
        Arguments.of(
            "CHE***286", List.of(
                new BurDto("***545", BETRIEBSZWEIGGEMEINSCHAFT))),
        Arguments.of(
            "CHE***186", List.of(
                new BurDto("***730", GANZJAHRESBETRIEB),
                new BurDto("***476", SOEMMERUNGSBETRIEB),
                new BurDto("***257", SOEMMERUNGSBETRIEB)
            )));
  }

  @ParameterizedTest
  @MethodSource("authorizedBurDataProvider")
  void testGetAuthorizedBursByUid(String uid, List<BurDto> expectedResult) {
    var actualResult = AuthTestUtils.requestAs(ADMIN).accept(ContentType.JSON).when()
        .get(UserController.PATH + "/uid/" + uid + "/authorized-burs").then().statusCode(200)
        .extract()
        .as(new TypeRef<List<BurDto>>() {
        });

    assertThat(actualResult).usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expectedResult);
  }

}
