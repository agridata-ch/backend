package integration.user;

import static ch.agridata.user.dto.FarmTypeEnum.BETRIEBSZWEIGGEMEINSCHAFT;
import static ch.agridata.user.dto.FarmTypeEnum.GANZJAHRESBETRIEB;
import static ch.agridata.user.dto.FarmTypeEnum.PRODUKTIONSSTAETTE;
import static ch.agridata.user.dto.FarmTypeEnum.SCHLACHTBETRIEB;
import static ch.agridata.user.dto.FarmTypeEnum.TIERHALTUNG;
import static integration.testutils.TestUserEnum.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.user.controller.UserController;
import ch.agridata.user.dto.BurDto;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.Bur;
import integration.testutils.TestDataIdentifiers.Uid;
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
            Uid.CHE101000001.name(), List.of(
                new BurDto(Bur.A99910002.name(), GANZJAHRESBETRIEB),
                new BurDto(Bur.A99910003.name(), PRODUKTIONSSTAETTE),
                new BurDto(Bur.A99910004.name(), PRODUKTIONSSTAETTE),
                new BurDto(Bur.A99910005.name(), PRODUKTIONSSTAETTE)
            )),
        Arguments.of(
            Uid.CHE102000001.name(), List.of(
                new BurDto(Bur.A99920004.name(), GANZJAHRESBETRIEB),
                new BurDto(Bur.A99920006.name(), TIERHALTUNG)
            )),
        Arguments.of(
            Uid.CHE102000002.name(), List.of(
                new BurDto(Bur.A99920005.name(), GANZJAHRESBETRIEB)
            )),
        Arguments.of(
            Uid.CHE103000001.name(), List.of(
                new BurDto(Bur.A99930004.name(), GANZJAHRESBETRIEB)
            )),
        Arguments.of(
            Uid.CHE103000002.name(), List.of(
                new BurDto(Bur.A99930005.name(), BETRIEBSZWEIGGEMEINSCHAFT)
            )),
        Arguments.of(
            Uid.CHE104000002.name(), List.of(
                new BurDto(Bur.A99940003.name(), SCHLACHTBETRIEB),
                new BurDto(Bur.A99940004.name(), SCHLACHTBETRIEB)
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
