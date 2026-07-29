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
import ch.agridata.user.dto.FarmTypeEnum;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.Bur;
import integration.testutils.TestDataIdentifiers.Uid;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@RequiredArgsConstructor
class BurAuthorizationTest {

  /** Start date of a direct farm-to-person or person-to-farm relation in the AGIS stubs. */
  private static final LocalDateTime RELATION_SINCE = LocalDateTime.of(2025, 12, 11, 8, 23, 31);

  /** Start date of a parent-child relation in the AGIS stubs; later than {@link #RELATION_SINCE}, so it wins for child farms. */
  private static final LocalDateTime PARENT_RELATION_SINCE = LocalDateTime.of(2025, 12, 11, 8, 23, 32);

  /**
   * Covers every UID that has AGIS register data. {@code Uid.ZZZ199984051} and {@code Uid.ZZZ199984068} are intentionally left out: they are
   * equine owners of another use case and have no AGIS register stub.
   */
  static Stream<Arguments> authorizedBurDataProvider() {
    return Stream.of(
        Arguments.of(
            Uid.CHE101000001.name(), List.of(
                // resolved via the farm-to-person relation
                bur(Bur.CODE_99910002.getCode(), GANZJAHRESBETRIEB, Uid.CHE101000001.name(), RELATION_SINCE),
                // resolved via its own farm parent relation
                bur(Bur.CODE_99910003.getCode(), PRODUKTIONSSTAETTE, Uid.CHE101000001.name(), PARENT_RELATION_SINCE),
                // resolved via the farm child relations of 99910002
                bur(Bur.CODE_99910004.getCode(), PRODUKTIONSSTAETTE, Uid.CHE101000001.name(), PARENT_RELATION_SINCE),
                bur(Bur.CODE_99910005.getCode(), PRODUKTIONSSTAETTE, Uid.CHE101000001.name(), PARENT_RELATION_SINCE)
            )),
        Arguments.of(
            Uid.CHE102000001.name(), List.of(
                bur(Bur.CODE_99920004.getCode(), GANZJAHRESBETRIEB, Uid.CHE102000001.name(), RELATION_SINCE),
                // resolved via its own farm parent relation
                bur(Bur.CODE_99920006.getCode(), TIERHALTUNG, Uid.CHE102000001.name(), PARENT_RELATION_SINCE)
            )),
        Arguments.of(
            Uid.CHE102000002.name(), List.of(
                bur(Bur.CODE_99920005.getCode(), GANZJAHRESBETRIEB, Uid.CHE102000002.name(), RELATION_SINCE)
            )),
        Arguments.of(
            Uid.CHE103000001.name(), List.of(
                bur(Bur.CODE_99930004.getCode(), GANZJAHRESBETRIEB, Uid.CHE103000001.name(), RELATION_SINCE)
            )),
        Arguments.of(
            Uid.CHE103000002.name(), List.of(
                bur(Bur.CODE_99930005.getCode(), BETRIEBSZWEIGGEMEINSCHAFT, Uid.CHE103000002.name(), RELATION_SINCE)
            )),
        Arguments.of(
            Uid.CHE104000002.name(), List.of(
                bur(Bur.CODE_99940003.getCode(), SCHLACHTBETRIEB, Uid.CHE104000002.name(), RELATION_SINCE),
                // no farm-to-person relation, resolved via the person-to-farm relation
                bur(Bur.CODE_99940004.getCode(), SCHLACHTBETRIEB, Uid.CHE104000002.name(), RELATION_SINCE)
            )));
  }

  private static BurDto bur(String bur, FarmTypeEnum farmType, String uid, LocalDateTime relationSince) {
    return BurDto.builder().bur(bur).farmTypeCode(farmType).uid(uid).relationSince(relationSince).build();
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
