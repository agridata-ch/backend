package integration.agreement;

import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static io.restassured.http.ContentType.JSON;

import ch.agridata.agreement.controller.DataRequestController;
import ch.agridata.agreement.dto.DataRequestDescriptionDto;
import ch.agridata.agreement.dto.DataRequestPurposeDto;
import ch.agridata.agreement.dto.DataRequestStateEnum;
import ch.agridata.agreement.dto.DataRequestTitleDto;
import ch.agridata.agreement.dto.DataRequestUpdateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import integration.testutils.TestUserEnum;
import io.restassured.response.Response;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import lombok.SneakyThrows;

public class DataRequestTestFactory {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static DataRequestUpdateDto.DataRequestUpdateDtoBuilder getPartialDataRequestUpdateDtoBuilder() {
    return DataRequestUpdateDto.builder()
        .description(DataRequestDescriptionDto.builder().de("Beschreibung lang genug").build())
        .purpose(new DataRequestPurposeDto("Zweck lang genug", "But assez long",
            "Scopo abbastanza lungo"))
        .products(List.of(TestDataIdentifiers.DataProduct.UUID_085E4B72.uuid()))
        .dataConsumerCity("Bern")
        .dataConsumerCountry("CH")
        .dataConsumerZip("3008")
        .dataConsumerStreet("Musterstrasse 1")
        .contactPhoneNumber("+41 79 123 45 67")
        .contactEmailAddress("email@test.com");
  }

  public static DataRequestUpdateDto.DataRequestUpdateDtoBuilder getDataRequestDto() {
    return getPartialDataRequestUpdateDtoBuilder()
        .title(new DataRequestTitleDto("Anfrage Titel", "Titre demande", "Titolo richiesta"))
        .targetGroup("Test Zielgruppe")
        .description(
            new DataRequestDescriptionDto("Beschreibung lang genug", "Description assez longue",
                "Descrizione abbastanza lunga"))
        .dataConsumerDisplayName("Test Consumer");
  }

  @SneakyThrows
  public static Response createDataRequest() {
    return AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).given()
        .contentType(JSON)
        .body(MAPPER.writeValueAsString(getPartialDataRequestUpdateDtoBuilder().build()))
        .when()
        .post(DataRequestController.PATH);
  }


  @SneakyThrows
  public static Response updateDataRequest(String requestId, DataRequestUpdateDto dto) {
    return AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).given()
        .contentType(JSON)
        .body(MAPPER.writeValueAsString(dto))
        .when()
        .put(DataRequestController.PATH + "/" + requestId);
  }


  @SneakyThrows
  public static Response setStatusAs(String requestId, DataRequestStateEnum stateCode, TestUserEnum user) {
    return AuthTestUtils.requestAs(user).given()
        .contentType(JSON)
        .body(MAPPER.writeValueAsString(stateCode))
        .when()
        .put(DataRequestController.PATH + "/" + requestId + "/status");
  }

  @SneakyThrows
  public static Response updateLogo(String requestId, String logoFileName) {
    File logo = new File("src/test/resources/data-request-logos/" + logoFileName);
    return AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).given()
        .multiPart("logo", logo, Files.probeContentType(logo.toPath()))
        .when()
        .put(DataRequestController.PATH + "/" + requestId + "/logo/");
  }
}
