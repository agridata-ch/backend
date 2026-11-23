package integration.user;

import static ch.agridata.user.dto.FarmTypeEnum.GANZJAHRESBETRIEB;
import static ch.agridata.user.dto.FarmTypeEnum.PRODUKTIONSSTAETTE;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.PRODUCER_A;
import static integration.testutils.TestUserEnum.PRODUCER_C;
import static integration.testutils.TestUserEnum.PRODUCER_E;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.user.controller.UserController;
import ch.agridata.user.dto.AddressDto;
import ch.agridata.user.dto.CompanyDto;
import ch.agridata.user.dto.FarmDto;
import ch.agridata.user.dto.FarmOverviewDto;
import com.github.tomakehurst.wiremock.client.WireMock;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.Bur;
import integration.testutils.TestDataIdentifiers.Uid;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
@ConnectWireMock
class FarmOverviewTest {

  private static final String PATH = UserController.PATH + "/uids/{uid}/farm-overview";

  WireMock wireMock;

  @BeforeEach
  void setUp() {
    wireMock.resetToDefaultMappings();
  }

  @Test
  void givenMultipleAuthorizedBurs_whenGetFarmOverview_thenReturnCompanyAndSortedFarms() {
    var result = AuthTestUtils.requestAs(PRODUCER_A).when().get(PATH, Uid.CHE101000001.name())
        .then().statusCode(200).extract().as(FarmOverviewDto.class);

    assertThat(result.company()).isEqualTo(CompanyDto.builder()
        .uid(Uid.CHE101000001.name())
        .name("Erika Musterfrau")
        .address(AddressDto.builder().street("Beispielgasse").houseNumber("42").zipCode("3001").town("Bern").build())
        .build());
    assertThat(result.farms()).extracting(FarmDto::bur).containsExactly(
        Bur.CODE_99910002.getCode(), Bur.CODE_99910003.getCode(), Bur.CODE_99910004.getCode(), Bur.CODE_99910005.getCode());
    assertThat(result.farms().getFirst()).isEqualTo(FarmDto.builder()
        .bur(Bur.CODE_99910002.getCode())
        .ktIdB("FLXXA0002")
        .farmTypeCode(GANZJAHRESBETRIEB)
        .address(AddressDto.builder().street("Beispielgasse").houseNumber("42").zipCode("3001").town("Bern").build())
        .build());
    assertThat(result.farms().get(1).farmTypeCode()).isEqualTo(PRODUKTIONSSTAETTE);
  }

  @Test
  void givenSingleAuthorizedBur_whenGetFarmOverview_thenReturnOneFarm() {
    var result = AuthTestUtils.requestAs(PRODUCER_C).when().get(PATH, Uid.CHE103000001.name())
        .then().statusCode(200).extract().as(FarmOverviewDto.class);

    assertThat(result.company().name()).isEqualTo("Max Mustermann");
    assertThat(result.farms()).containsExactly(FarmDto.builder()
        .bur(Bur.CODE_99930004.getCode())
        .ktIdB("FLXXC0004")
        .farmTypeCode(GANZJAHRESBETRIEB)
        .address(AddressDto.builder().street("Musterweg 1").zipCode("3001").town("Bern").build())
        .build());
  }

  @Test
  void givenAdmin_whenGetFarmOverview_thenNameIsTakenFromAgis() {
    var result = AuthTestUtils.requestAs(ADMIN).when().get(PATH, Uid.CHE103000001.name())
        .then().statusCode(200).extract().as(FarmOverviewDto.class);

    assertThat(result.company().name()).isEqualTo("Max Mustermann");
    assertThat(result.farms()).extracting(FarmDto::bur).containsExactly(Bur.CODE_99930004.getCode());
  }

  @Test
  void givenEquidOwnerUid_whenGetFarmOverview_thenReturnCompanyWithoutFarms() {
    var result = AuthTestUtils.requestAs(PRODUCER_E).when().get(PATH, Uid.ZZZ199984068.name())
        .then().statusCode(200).extract().as(FarmOverviewDto.class);

    assertThat(result).isEqualTo(new FarmOverviewDto(
        CompanyDto.builder().uid(Uid.ZZZ199984068.name()).name("Nico Demomann").build(), List.of()));
  }

  @Test
  void givenAdminAndUnknownUid_whenGetFarmOverview_thenNotFound() {
    AuthTestUtils.requestAs(ADMIN).when().get(PATH, "CHE999999999")
        .then().statusCode(404);
  }

  @Test
  void givenAdminAndMalformedUid_whenGetFarmOverview_thenBadRequest() {
    AuthTestUtils.requestAs(ADMIN).when().get(PATH, "garbage")
        .then().statusCode(400);
  }

  @Test
  void givenUidNotAuthorizedForProducer_whenGetFarmOverview_thenForbidden() {
    AuthTestUtils.requestAs(PRODUCER_A).when().get(PATH, Uid.CHE103000001.name())
        .then().statusCode(403);
  }
}
