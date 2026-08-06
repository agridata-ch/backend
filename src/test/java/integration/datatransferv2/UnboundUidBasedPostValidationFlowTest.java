package integration.datatransferv2;

import static integration.testutils.TestUserEnum.CONSUMER_BLV_1;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_WITHOUT_UID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.datatransferv2.controller.DataTransferController;
import ch.agridata.product.persistence.DataProductEntity;
import com.github.tomakehurst.wiremock.client.WireMock;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.DataProduct;
import integration.testutils.TestDataIdentifiers.Identifier;
import integration.testutils.TestDataIdentifiers.Uid;
import integration.testutils.TestUserEnum;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@RequiredArgsConstructor
@ConnectWireMock
class UnboundUidBasedPostValidationFlowTest {

  private static final String UELN = "756001234567890";
  private static final String RECIPIENT_UID = "CHE123456789";
  private static final String CONSENT_NOT_GRANTED_MSG = "Consent not granted for the requested data producer(s): ";
  private static final String UIDS_HEADER_MISSING_MSG =
      "AGRIDATA-RESPONSE-PRODUCER-UIDS header is not present in provider response";
  // Consumer UID returned by the provider in the response header when CONSUMER_BLV_WITHOUT_UID calls without a UID in token
  private static final String CONSUMER_BLV_WITHOUT_UID_CONSUMER_UID = "CHE403244345";

  private static final Identifier<DataProductEntity> PRODUCT_UID_BASED = DataProduct.UUID_593913AC;

  WireMock wireMock;

  static Stream<Arguments> testCases() {
    // Consents GRANTED for data request 218bca06 (covers this equid product): CHE101000001, CHE103000001
    record Case(String desc, List<String> uids, Map<String, String> params, int status, String msg) {
    }

    var cases = List.of(

        // --- 200: consent granted -----------------------------------------------------------------

        new Case("single UID CHE101000001 with consent",
            List.of(Uid.CHE101000001.name()), Map.of(), 200, null),
        new Case("single UID CHE103000001 with consent",
            List.of(Uid.CHE103000001.name()), Map.of(), 200, null),
        new Case("two UIDs, both with consent",
            List.of(Uid.CHE101000001.name(), Uid.CHE103000001.name()), Map.of(), 200, null),

        // UIDs are not date-filtered, so a historic requested date still yields granted consent
        new Case("UIDs with consent remain granted at historic date",
            List.of(Uid.CHE101000001.name(), Uid.CHE103000001.name()),
            Map.of("date", "1999-01-01"), 200, null),

        // --- 200: no data (present-but-empty header), no consent check ----------------------------

        new Case("UIDs header present but empty",
            List.of(), Map.of(), 200, null),

        // --- 502: required header not present -----------------------------------------------------

        new Case("UIDs header not present",
            null, Map.of(), 502, UIDS_HEADER_MISSING_MSG),

        // --- 403: consent missing -----------------------------------------------------------------

        new Case("single UID has no consent",
            List.of(Uid.CHE102000001.name()), Map.of(), 403,
            CONSENT_NOT_GRANTED_MSG + Uid.CHE102000001.name()),
        new Case("one of multiple UIDs has no consent",
            List.of(Uid.CHE101000001.name(), Uid.CHE102000001.name()), Map.of(), 403,
            CONSENT_NOT_GRANTED_MSG + Uid.CHE102000001.name())
    );

    // Run all cases for both users: CONSUMER_BLV_1 (UID in token) and CONSUMER_BLV_WITHOUT_UID (no UID in token).
    // Both must yield identical results, covering both code paths of UnboundUidBasedPostValidationFlow.
    return Stream.of(CONSUMER_BLV_1, CONSUMER_BLV_WITHOUT_UID)
        .flatMap(user -> cases.stream()
            .map(c -> Arguments.of(user, c.desc(), c.uids(), c.params(), c.status(), c.msg())));
  }

  @ParameterizedTest(name = "[{index}] [{0}] {1}")
  @MethodSource("testCases")
  void givenResponseHeaders_whenProductRequested_thenExpectedStatusReturned(
      TestUserEnum testUser,
      String description,
      List<String> responseUids,
      Map<String, String> optionalQueryParams,
      int expectedStatus, String expectedDebugMessage) {
    mockResponseHeaders(responseUids);

    var request = AuthTestUtils.requestAs(testUser)
        .pathParam("productId", PRODUCT_UID_BASED.uuid())
        .queryParam("ueln", UELN)
        .queryParam("recipientUid", RECIPIENT_UID);
    optionalQueryParams.forEach(request::queryParam);

    var responseSpec = request
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(expectedStatus);

    if (expectedDebugMessage != null) {
      responseSpec.body("debugMessage", equalTo(expectedDebugMessage));
    } else {
      responseSpec.header("AGRIDATA-REQUEST-ID", notNullValue());
    }

    var expectedUrl = "/tvd/animal-tracing/v1.0/equid/shared-data/equids/" + UELN
        + "?dataPackage=TVD_EquidDetailV1&recipientUid=" + RECIPIENT_UID
        + optionalQueryParams.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(e -> "&" + e.getKey() + "=" + e.getValue())
        .collect(Collectors.joining());
    wireMock.verifyThat(1, WireMock.getRequestedFor(WireMock.urlEqualTo(expectedUrl)));
  }

  void mockResponseHeaders(List<String> uids) {
    var baseResponse = WireMock.aResponse().withStatus(200);
    if (uids != null) {
      baseResponse = baseResponse.withHeader("AGRIDATA-RESPONSE-PRODUCER-UIDS", String.join(",", uids));
    }

    // When the consumer UID is already known (present in request header), no CONSUMER-UID response header is needed.
    wireMock.register(WireMock.get(WireMock.urlPathMatching(".*/tvd/.*"))
        .withHeader("AGRIDATA-CONSUMER-UID", WireMock.matching(".+"))
        .willReturn(baseResponse));

    // When the consumer UID is absent from the request, the provider returns it in the response header.
    wireMock.register(WireMock.get(WireMock.urlPathMatching(".*/tvd/.*"))
        .withHeader("AGRIDATA-CONSUMER-UID", WireMock.absent())
        .willReturn(baseResponse.withHeader("AGRIDATA-CONSUMER-UID", CONSUMER_BLV_WITHOUT_UID_CONSUMER_UID)));
  }
}
