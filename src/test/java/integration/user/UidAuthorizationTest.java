package integration.user;

import static ch.agridata.user.dto.LegalFormEnum.AKTIENGESELLSCHAFT;
import static ch.agridata.user.dto.LegalFormEnum.EINFACHE_GESELLSCHAFT;
import static ch.agridata.user.dto.LegalFormEnum.NATUERLICHE_PERSON;
import static integration.testutils.TestDataIdentifiers.Uid.CHE102000001;
import static integration.testutils.TestDataIdentifiers.Uid.CHE102000002;
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
            "FLXXA0001", List.of(
                new UidDto("CHE101000001", "Erika Musterfrau", NATUERLICHE_PERSON))),
        Arguments.of(
            "FLXXB0001", List.of(
                new UidDto("CHE102000001", "Jonas Testmann", NATUERLICHE_PERSON),
                new UidDto("CHE102000002", "Testpartner GmbH", EINFACHE_GESELLSCHAFT))),
        Arguments.of(
            "FLXXB0003", List.of(
                new UidDto("CHE102000002", "Testpartner GmbH", EINFACHE_GESELLSCHAFT))),
        Arguments.of(
            "FLXXC0001", List.of(
                new UidDto("CHE103000001", "Max Mustermann", NATUERLICHE_PERSON),
                new UidDto("CHE103000002", "Testfirma GmbH", EINFACHE_GESELLSCHAFT))),
        Arguments.of(
            "FLXXD0001", List.of(
                new UidDto("CHE104000002", "Testbetrieb AG", AKTIENGESELLSCHAFT))),
        // For some cantons, the AGIS Register API does not provide the person's UID in the person object,
        // but only in the farm object. The following test covers this scenario.
        Arguments.of(
            "FLXXB0001", List.of(
                new UidDto("CHE102000001", "Jonas Testmann", NATUERLICHE_PERSON),
                new UidDto("CHE102000002", "Testpartner GmbH", EINFACHE_GESELLSCHAFT)))
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
                new UidDto(CHE102000001.name(), "Jonas Testmann", NATUERLICHE_PERSON),
                new UidDto(CHE102000002.name(), "Testpartner GmbH", EINFACHE_GESELLSCHAFT)));
  }

}
