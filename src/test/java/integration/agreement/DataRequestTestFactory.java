package integration.agreement;

import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static io.restassured.http.ContentType.JSON;

import ch.agridata.agreement.controller.ContractRevisionController;
import ch.agridata.agreement.controller.DataRequestController;
import ch.agridata.agreement.dto.DataRequestDescriptionDto;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.DataRequestPurposeDto;
import ch.agridata.agreement.dto.DataRequestStateEnum;
import ch.agridata.agreement.dto.DataRequestTitleDto;
import ch.agridata.agreement.dto.DataRequestUpdateDto;
import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.dto.VerifyOtpRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import integration.testutils.TestUserEnum;
import io.restassured.response.Response;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
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
    return createDataRequestAs(getPartialDataRequestUpdateDtoBuilder().build(), CONSUMER_BIO_SUISSE);
  }

  @SneakyThrows
  public static Response createDataRequestAs(TestUserEnum user) {
    return createDataRequestAs(getPartialDataRequestUpdateDtoBuilder().build(), user);
  }

  @SneakyThrows
  public static Response createDataRequestAs(DataRequestUpdateDto dto, TestUserEnum user) {
    return AuthTestUtils.requestAs(user).given()
        .contentType(JSON)
        .body(MAPPER.writeValueAsString(dto))
        .when()
        .post(DataRequestController.PATH_V1);
  }


  public static Response updateDataRequest(String requestId, DataRequestUpdateDto dto) {
    return updateDataRequestAs(requestId, dto, CONSUMER_BIO_SUISSE);
  }

  @SneakyThrows
  public static Response updateDataRequestAs(String requestId, DataRequestUpdateDto dto, TestUserEnum user) {
    return AuthTestUtils.requestAs(user).given()
        .contentType(JSON)
        .body(MAPPER.writeValueAsString(dto))
        .when()
        .put(DataRequestController.PATH_V1 + "/" + requestId);
  }

  @SneakyThrows
  public static Response setStatusAs(String requestId, DataRequestStateEnum stateCode, TestUserEnum user) {
    return AuthTestUtils.requestAs(user).given()
        .contentType(JSON)
        .body(MAPPER.writeValueAsString(stateCode))
        .when()
        .put(DataRequestController.PATH_V1 + "/" + requestId + "/status");
  }

  @SneakyThrows
  public static Response requestOtpChallengeAs(String contractRevisionId, SignatureSlotCodeEnum slotCode, TestUserEnum user) {
    return AuthTestUtils.requestAs(user).given()
        .pathParam("id", contractRevisionId)
        .pathParam("slotCode", slotCode.name())
        .post(ContractRevisionController.PATH + "/{id}/signatures/{slotCode}/otp-challenges");
  }

  @SneakyThrows
  public static Response signContractRevisionAs(
      String contractRevisionId,
      SignatureSlotCodeEnum slotCode,
      UUID challengeId,
      String otpCode,
      TestUserEnum user) {
    return AuthTestUtils.requestAs(user).given()
        .contentType("application/json")
        .pathParam("id", contractRevisionId)
        .pathParam("slotCode", slotCode.name())
        .pathParam("challengeId", challengeId.toString())
        .body(new VerifyOtpRequestDto(otpCode))
        .when()
        .post(ContractRevisionController.PATH + "/{id}/signatures/{slotCode}/otp-challenges/{challengeId}/verification");
  }

  @SneakyThrows
  public static Response updateValidRedirectUriRegex(String requestId, String regex, TestUserEnum user) {
    return AuthTestUtils.requestAs(user).given()
        .contentType(JSON)
        .body(MAPPER.writeValueAsString(java.util.Map.of("validRedirectUriRegex", regex)))
        .when()
        .put(DataRequestController.PATH_V1 + "/" + requestId + "/valid-redirect-uri-regex");
  }

  public static Response updateLogo(String requestId, String logoFileName) {
    return updateLogoAs(requestId, logoFileName, CONSUMER_BIO_SUISSE);
  }

  @SneakyThrows
  public static Response updateLogoAs(String requestId, String logoFileName, TestUserEnum user) {
    File logo = new File("src/test/resources/data-request-logos/" + logoFileName);
    return AuthTestUtils.requestAs(user).given()
        .multiPart("logo", logo, Files.probeContentType(logo.toPath()))
        .when()
        .put(DataRequestController.PATH_V1 + "/" + requestId + "/logo/");
  }

  public static DataRequestDto createReadyForSigningDataRequestFor(TestUserEnum user) {
    Response createResponse = createDataRequestAs(getDataRequestDto().build(), user);
    DataRequestDto created = createResponse.as(DataRequestDto.class);
    String requestId = created.id().toString();
    setStatusAs(requestId, DataRequestStateEnum.IN_REVIEW, user);
    Response toBeSignedResponse = setStatusAs(requestId, DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER, ADMIN);
    return toBeSignedResponse.as(DataRequestDto.class);
  }

  public static UUID createContractRevisionAndReturnId() {
    return createReadyForSigningDataRequestFor(CONSUMER_BIO_SUISSE).currentContractRevisionId();
  }
}
