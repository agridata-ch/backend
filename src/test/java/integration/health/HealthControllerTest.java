package integration.health;

import static integration.testutils.TestUserEnum.CONSUMER_BLV_1;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.common.dto.TranslationDto;
import ch.agridata.health.controller.HealthController;
import ch.agridata.health.dto.HealthDto;
import ch.agridata.health.dto.HealthDto.DataProviderStatus;
import ch.agridata.health.dto.HealthDto.HealthStatus;
import com.github.tomakehurst.wiremock.client.WireMock;
import integration.testutils.AuthTestUtils;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@ConnectWireMock
@RequiredArgsConstructor
class HealthControllerTest {

  WireMock wireMock;

  private static final String AGIS_REGISTER_HEALTH = "/agis/register-data/2/health";
  private static final String AGIS_STRUCTURE_HEALTH = "/agis/structure-data/2/health";
  private static final String AGIS_ECO_ETHO_HEALTH = "/agis/eco-etho-data/2/health";
  private static final String TVD_HEALTH = "/tvd/animal-tracing/health-check";

  @Test
  void givenAgisUpAndTvdDown_whenGetHealth_thenReportsAgisUpTvdDown() {
    stub(AGIS_REGISTER_HEALTH, 200);
    stub(AGIS_STRUCTURE_HEALTH, 200);
    stub(AGIS_ECO_ETHO_HEALTH, 200);
    stub(TVD_HEALTH, 503);

    assertResponse(HealthStatus.UP, HealthStatus.DOWN);
  }

  @Test
  void givenAgisDownAndTvdUp_whenGetHealth_thenReportsAgisDownTvdUp() {
    stub(AGIS_REGISTER_HEALTH, 200);
    stub(AGIS_STRUCTURE_HEALTH, 200);
    stub(AGIS_ECO_ETHO_HEALTH, 503);
    stub(TVD_HEALTH, 200);

    assertResponse(HealthStatus.DOWN, HealthStatus.UP);
  }

  private void stub(String url, int status) {
    wireMock.register(WireMock.get(WireMock.urlEqualTo(url))
        .willReturn(WireMock.aResponse().withStatus(status)));
  }

  private void assertResponse(HealthStatus agisStatus, HealthStatus tvdStatus) {
    var response = AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .when().get(HealthController.PATH)
        .then().statusCode(200)
        .extract().as(HealthDto.class);

    assertThat(response.agridataStatus()).isEqualTo(HealthStatus.UP);
    assertThat(response.dataProviders()).containsExactlyInAnyOrder(
        new DataProviderStatus(
            new TranslationDto("BLW", "OFAG", "UFAG"),
            new TranslationDto("AGIS", "SIPA", "AGIS"),
            agisStatus),
        new DataProviderStatus(
            new TranslationDto("Identitas", "Identitas", "Identitas"),
            new TranslationDto("TVD", "BDTA", "BDTA"),
            tvdStatus),
        new DataProviderStatus(
            new TranslationDto("Identitas", "Identitas", "Identitas"),
            new TranslationDto("ZO-API", "ZO-API", "ZO-API"),
            HealthStatus.HEALTH_CHECK_NOT_IMPLEMENTED),
        new DataProviderStatus(
            new TranslationDto("BLW", "OFAG", "UFAG"),
            new TranslationDto("Acontrol", "Acontrol", "Acontrol"),
            HealthStatus.HEALTH_CHECK_NOT_IMPLEMENTED),
        new DataProviderStatus(
            new TranslationDto("BLV", "OSAV", "USAV"),
            new TranslationDto("Acontrol", "Acontrol", "Acontrol"),
            HealthStatus.HEALTH_CHECK_NOT_IMPLEMENTED));
  }
}
