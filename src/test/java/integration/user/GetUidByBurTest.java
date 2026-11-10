package integration.user;

import static integration.testutils.TestUserEnum.CONSUMER_BLV_1;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.user.controller.UserController;
import ch.agridata.user.dto.UidDto;
import com.github.tomakehurst.wiremock.client.WireMock;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.Bur;
import integration.testutils.TestDataIdentifiers.Uid;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
@ConnectWireMock
class GetUidByBurTest {

  private static final String PATH = UserController.PATH + "/burs/{bur}/uid";
  private static final String BUR = Bur.CODE_99920004.getCode();

  WireMock wireMock;

  @BeforeEach
  void setUp() {
    wireMock.resetToDefaultMappings();
  }

  @Test
  void givenKnownBur_whenGetUidByBur_thenReturnUid() {
    stubAgisFarms(BUR, "{ \"ktIdB\": \"FLTEST0001\", \"agisIdB\": 1, \"ber\": \"%s\", \"uid\": \"%s\" }".formatted(BUR, Uid.CHE102000001.name()));

    var result = AuthTestUtils.requestAs(CONSUMER_BLV_1).when().get(PATH, BUR)
        .then().statusCode(200).extract().as(UidDto.class);

    assertThat(result.uid()).isEqualTo(Uid.CHE102000001.name());
  }

  @Test
  void givenUnknownBur_whenGetUidByBur_thenNotFound() {
    stubAgisFarms(BUR, "");

    AuthTestUtils.requestAs(CONSUMER_BLV_1).when().get(PATH, BUR).then().statusCode(404);
  }

  @Test
  void givenInvalidBur_whenGetUidByBur_thenBadRequest() {
    AuthTestUtils.requestAs(CONSUMER_BLV_1).when().get(PATH, "invalid-bur").then().statusCode(400);
  }

  private void stubAgisFarms(String bur, String farmsJson) {
    wireMock.register(WireMock.post(WireMock.urlEqualTo("/agis/register-data/2/register"))
        .withRequestBody(WireMock.matchingJsonPath("$.farmSearchParameters.ber", WireMock.equalTo(bur)))
        .atPriority(1)
        .willReturn(WireMock.okJson("""
            {
              "personFarmTree": {
                "relevantPersons": {},
                "personRelations": {},
                "farmRelations": {},
                "relevantFarms": { "farm": [ %s ] }
              }
            }""".formatted(farmsJson))));
  }
}
