package integration.user;

import static ch.agridata.user.dto.LegalFormEnum.EINFACHE_GESELLSCHAFT;
import static ch.agridata.user.dto.LegalFormEnum.NATUERLICHE_PERSON;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.PRODUCER_AGIS_NO_UID_TVD_UNAVAILABLE;
import static integration.testutils.TestUserEnum.PRODUCER_AGIS_TVD_UNAVAILABLE;
import static integration.testutils.TestUserEnum.PRODUCER_AGIS_UNAVAILABLE_TVD_NO_UID;
import static integration.testutils.TestUserEnum.PRODUCER_B;
import static integration.testutils.TestUserEnum.PRODUCER_BOTH_NO_UID;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.common.dto.ExceptionDto;
import ch.agridata.common.dto.ExceptionEnum;
import ch.agridata.common.dto.ExternalServiceExceptionDto;
import ch.agridata.user.controller.UserController;
import ch.agridata.user.dto.UidDto;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestUserEnum;
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
        Arguments.of(TestUserEnum.PRODUCER_A),
        Arguments.of(TestUserEnum.PRODUCER_B),
        Arguments.of(TestUserEnum.PRODUCER_B_3),
        Arguments.of(TestUserEnum.PRODUCER_C),
        Arguments.of(TestUserEnum.PRODUCER_D),
        Arguments.of(TestUserEnum.PRODUCER_E)
    );
  }

  @ParameterizedTest
  @MethodSource("authorizedUidDataProvider")
  void givenProducer_whenGetAuthorizedUids_thenReturnCorrectUids(TestUserEnum testUser) {
    var actualResult = AuthTestUtils.requestAs(testUser).accept(ContentType.JSON).when()
        .get(UserController.PATH + "/authorized-uids").then().statusCode(200)
        .extract()
        .as(new TypeRef<List<UidDto>>() {
        });

    assertThat(actualResult).usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(testUser.getCompanyUids().stream()
                       .map(uid -> new UidDto(uid.name(), uid.getUidName(), uid.getLegalForm()))
                       .toList());
  }

  @Test
  void givenAdmin_whenGetAuthorizedUidsForProducer_thenReturnCorrectUids() {
    var actualResult = AuthTestUtils.requestAs(ADMIN).accept(ContentType.JSON).when()
        .get(UserController.PATH + "/authorized-uids"
                 + "?kt-id-p=" + PRODUCER_B.getKtIdP()
                 + "&agate-login-id=" + PRODUCER_B.getAgateLoginId()
        ).then().statusCode(200)
        .extract()
        .as(new TypeRef<List<UidDto>>() {
        });

    assertThat(actualResult).usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(PRODUCER_B.getCompanyUids().stream()
                       .map(uid -> new UidDto(uid.name(), uid.getUidName(), uid.getLegalForm()))
                       .toList());
  }

  // For some cantons, the AGIS Register API does not provide the person's UID in the person object,
  // but only in the farm object. The following test covers this scenario.
  @Test
  void givenIncompleteAgisRegisterResponse_whenGetAuthorizedUidsForProducer_thenReturnCorrectUids() {
    var actualResult = AuthTestUtils.requestAs(ADMIN).accept(ContentType.JSON).when()
        .get(UserController.PATH + "/authorized-uids"
                 + "?kt-id-p=KtIdPWithMissingUidInPerson"
        ).then().statusCode(200)
        .extract()
        .as(new TypeRef<List<UidDto>>() {
        });

    assertThat(actualResult).usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(List.of(
            new UidDto("CHE102000001", "Jonas Testmann", NATUERLICHE_PERSON),
            new UidDto("CHE102000002", "Testpartner GmbH", EINFACHE_GESELLSCHAFT)
        ));
  }

  static Stream<Arguments> unavailableServiceDataProvider() {
    return Stream.of(
        Arguments.of(PRODUCER_AGIS_TVD_UNAVAILABLE),
        Arguments.of(PRODUCER_AGIS_NO_UID_TVD_UNAVAILABLE),
        Arguments.of(PRODUCER_AGIS_UNAVAILABLE_TVD_NO_UID)
    );
  }

  @ParameterizedTest
  @MethodSource("unavailableServiceDataProvider")
  void givenNoUidAndUnavailableServices_whenGetAuthorizedUidsForProducer_thenReturnBadGateway(TestUserEnum testUser) {
    var actualResult = AuthTestUtils.requestAs(testUser).accept(ContentType.JSON).when()
        .get(UserController.PATH + "/authorized-uids"
                 + "?kt-id-p=" + testUser.getKtIdP()
                 + "&agate-login-id=" + testUser.getAgateLoginId())
        .then().statusCode(504)
        .extract()
        .as(new TypeRef<ExternalServiceExceptionDto>() {
        });

    assertThat(actualResult.type()).isEqualTo(ExceptionEnum.EXTERNAL_SERVICE_ERROR);
  }

  @Test
  void givenNoUidAndBothServicesAvailable_whenGetAuthorizedUidsForProducer_thenReturnUidMissing() {
    var actualResult = AuthTestUtils.requestAs(PRODUCER_BOTH_NO_UID).accept(ContentType.JSON).when()
        .get(UserController.PATH + "/authorized-uids"
                 + "?kt-id-p=" + PRODUCER_BOTH_NO_UID.getKtIdP()
                 + "&agate-login-id=" + PRODUCER_BOTH_NO_UID.getAgateLoginId())
        .then().statusCode(502)
        .extract()
        .as(new TypeRef<ExceptionDto>() {
        });

    assertThat(actualResult.type()).isEqualTo(ExceptionEnum.UID_MISSING);
  }

}
