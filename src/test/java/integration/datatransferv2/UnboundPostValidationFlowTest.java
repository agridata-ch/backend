package integration.datatransferv2;

import static integration.testutils.TestUserEnum.CONSUMER_BLV_1;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_WITHOUT_UID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.datatransferv2.controller.DataTransferController;
import ch.agridata.product.persistence.DataProductEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.Bur;
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
import lombok.SneakyThrows;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@RequiredArgsConstructor
@ConnectWireMock
class UnboundPostValidationFlowTest {

  private static final String EARTAG_NUMBER = "123456";
  private static final String RECIPIENT_UID = "CHE123456789";
  private static final String CONSENT_NOT_GRANTED_MSG = "Consent not granted for the requested data producer(s): ";
  // Consumer UID returned by the provider in the response header when CONSUMER_BLV_WITHOUT_UID calls without a UID in token
  private static final String CONSUMER_BLV_WITHOUT_UID_CONSUMER_UID = "CHE403244345";

  private static final Identifier<DataProductEntity> PRODUCT_UNBOUND_POST_VALIDATION = DataProduct.UUID_6319423C;

  private final Flyway flyway;
  private final ObjectMapper objectMapper;

  WireMock wireMock;

  @BeforeEach
  void setUp() {
    // will make sure testdata prior to executing each test
    flyway.migrate();
    wireMock.resetToDefaultMappings();
  }

  static Stream<Arguments> testCases() {
    // Consents GRANTED for data request dc7dbc72: CHE101000001/99910002, CHE101000001/99910003, CHE103000001/99930004
    record Case(String desc, List<String> uids, List<String> burs, Map<String, String> params, int status, String msg) {
    }

    var cases = List.of(

        // --- 200: consent granted -----------------------------------------------------------------

        new Case("single UID with consent",
            List.of(Uid.CHE101000001.name()), null, Map.of(), 200, null),
        new Case("single UID CHE103000001 with consent",
            List.of(Uid.CHE103000001.name()), null, Map.of(), 200, null),
        new Case("single BUR 99910002 with consent",
            null, List.of(Bur._99910002.getCode()), Map.of(), 200, null),
        new Case("single BUR 99910003 with consent",
            null, List.of(Bur._99910003.getCode()), Map.of(), 200, null),
        new Case("UID and BUR both with consent",
            List.of(Uid.CHE101000001.name()), List.of(Bur._99910003.getCode()), Map.of(), 200, null),
        new Case("multiple UIDs and BURs, all with consent",
            List.of(Uid.CHE101000001.name(), Uid.CHE103000001.name()),
            List.of(Bur._99910003.getCode(), Bur._99930004.getCode()),
            Map.of(), 200, null),
        new Case("two BURs without UIDs, both with consent",
            null, List.of(Bur._99910002.getCode(), Bur._99910003.getCode()), Map.of(), 200, null),

        // --- 403: consent missing -----------------------------------------------------------------

        new Case("single UID has no consent",
            List.of(Uid.CHE102000001.name()), null, Map.of(), 403,
            CONSENT_NOT_GRANTED_MSG + Uid.CHE102000001.name()),
        new Case("one of multiple UIDs has no consent",
            List.of(Uid.CHE101000001.name(), Uid.CHE102000001.name()), null, Map.of(), 403,
            CONSENT_NOT_GRANTED_MSG + Uid.CHE102000001.name()),
        new Case("single BUR has no consent",
            null, List.of(Bur._99910004.getCode()), Map.of(), 403,
            CONSENT_NOT_GRANTED_MSG + Bur._99910004.getCode()),
        new Case("one of multiple BURs has no consent",
            List.of(Uid.CHE101000001.name(), Uid.CHE103000001.name()),
            List.of(Bur._99910003.getCode(), Bur._99910004.getCode()),
            Map.of(), 403,
            CONSENT_NOT_GRANTED_MSG + Bur._99910004.getCode()),
        new Case("UID without consent alongside BUR with consent",
            List.of(Uid.CHE102000001.name()), List.of(Bur._99910003.getCode()), Map.of(), 403,
            CONSENT_NOT_GRANTED_MSG + Uid.CHE102000001.name()),
        new Case("one UID and one BUR each without consent",
            List.of(Uid.CHE101000001.name(), Uid.CHE102000001.name()),
            List.of(Bur._99910003.getCode(), Bur._99910004.getCode()),
            Map.of(), 403,
            CONSENT_NOT_GRANTED_MSG + Uid.CHE102000001.name()),

        // --- 403: consent outdated ----------------------------------------------------------------

        // uid_bur_relation_since=2021-05-08 for all dc7dbc72 consents; UIDs are not date-filtered,
        // BURs are – so only the BURs appear in the debug message when a historic date is given
        new Case("all BUR consents outdated at historic date",
            List.of(Uid.CHE101000001.name(), Uid.CHE103000001.name()),
            List.of(Bur._99910003.getCode(), Bur._99930004.getCode()),
            Map.of("date", "2000-01-01"), 403,
            CONSENT_NOT_GRANTED_MSG + String.join(", ", Bur._99910003.getCode(), Bur._99930004.getCode()))
    );

    // Run all cases for both users: CONSUMER_BLV_1 (UID in token) and CONSUMER_BLV_WITHOUT_UID (no UID in token).
    // Both must yield identical results, covering both code paths of UnboundPostValidationFlow.
    return Stream.of(CONSUMER_BLV_1, CONSUMER_BLV_WITHOUT_UID)
        .flatMap(user -> cases.stream()
            .map(c -> Arguments.of(user, c.desc(), c.uids(), c.burs(), c.params(), c.status(), c.msg())));
  }

  @ParameterizedTest(name = "[{index}] [{0}] {1}")
  @MethodSource("testCases")
  void givenResponseHeaders_whenProductRequested_thenExpectedStatusReturned(
      TestUserEnum testUser,
      String description,
      List<String> responseUids, List<String> responseBurs,
      Map<String, String> optionalQueryParams,
      int expectedStatus, String expectedDebugMessage) {
    mockResponseHeaders(responseUids, responseBurs);

    var request = AuthTestUtils.requestAs(testUser)
        .pathParam("productId", PRODUCT_UNBOUND_POST_VALIDATION.uuid())
        .queryParam("eartagNumber", EARTAG_NUMBER)
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

    var expectedUrl = "/tvd/animal-tracing/v1.0/animal/shared-data/cattle/" + EARTAG_NUMBER
        + "?dataPackage=TVD_CattleDetailV1&recipientUid=" + RECIPIENT_UID
        + optionalQueryParams.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(e -> "&" + e.getKey() + "=" + e.getValue())
        .collect(Collectors.joining());
    wireMock.verifyThat(1, WireMock.getRequestedFor(WireMock.urlEqualTo(expectedUrl)));
  }

  @SneakyThrows
  void mockResponseHeaders(List<String> uids, List<String> burs) {
    var baseResponse = WireMock.aResponse().withStatus(200);
    if (uids != null) {
      baseResponse = baseResponse.withHeader("AGRIDATA-RESPONSE-PRODUCER-UIDS", objectMapper.writeValueAsString(uids));
    }
    if (burs != null) {
      baseResponse = baseResponse.withHeader("AGRIDATA-RESPONSE-PRODUCER-BURS", objectMapper.writeValueAsString(burs));
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
