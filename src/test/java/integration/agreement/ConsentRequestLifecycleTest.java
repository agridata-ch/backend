package integration.agreement;

import static ch.agridata.agreement.dto.ConsentRequestStateEnum.DECLINED;
import static ch.agridata.agreement.dto.ConsentRequestStateEnum.GRANTED;
import static ch.agridata.agreement.dto.ConsentRequestStateEnum.OPENED;
import static integration.testutils.TestDataIdentifiers.DataRequest.ACONTROL_BIO_SUISSE;
import static integration.testutils.TestUserEnum.PRODUCER_B;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import ch.agridata.agreement.controller.ConsentRequestAggregationController;
import ch.agridata.agreement.controller.ConsentRequestController;
import ch.agridata.agreement.dto.ConsentRequestAggregationDto;
import ch.agridata.agreement.dto.ConsentRequestAggregationStateEnum;
import ch.agridata.agreement.dto.ConsentRequestAggregationSummaryDto;
import ch.agridata.agreement.dto.ConsentRequestCreatedDto;
import ch.agridata.agreement.dto.ConsentRequestProducerViewV2Dto;
import ch.agridata.agreement.dto.ConsentRequestStateEnum;
import ch.agridata.agreement.dto.CreateConsentRequestDto;
import ch.agridata.agreement.job.ConsentRequestCleanupJob;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.Bur;
import integration.testutils.TestDataIdentifiers.Uid;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

/**
 * End-to-end lifecycle test of the consent-request logic for a data request with BUR based products. It drives creation, status updates
 * (including a forbidden direct edit of the derived UID row), the GRANTED/DECLINED roll-up precedence, and AGIS-triggered termination via
 * the cleanup job — verifying throughout only through the public REST endpoints.
 *
 * <p>Fixture: {@link Uid#CHE102000001} owns two BURs ({@link Bur#CODE_99920004}, {@link Bur#CODE_99920006}),
 * {@link Uid#CHE102000002} owns one ({@link Bur#CODE_99920005}); all authorized through the default AGIS register stubs. The ACONTROL data
 * request has no seeded consent rows for these UIDs, so every row this test asserts on is created by the test itself.
 *
 * @CommentLastReviewed 2026-08-21
 */
@QuarkusTest
@RequiredArgsConstructor
@ConnectWireMock
class ConsentRequestLifecycleTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String AGIS_MUTATION_URL = "/agis/register-data/2/registermutation";

  private final ConsentRequestCleanupJob cleanupJob;

  WireMock wireMock;

  @Test
  void givenBurBasedDataRequest_whenCreatedUpdatedAndCleanedUp_thenRollupAndTerminationAreReflectedInAggregations() {
    // PHASE 1 — create: one UID row plus one BUR row per authorized BUR, all OPENED.
    var created = createAcontrolConsents();
    assertThat(created).hasSize(5)
        .extracting(
            ConsentRequestCreatedDto::dataProducerUid,
            ConsentRequestCreatedDto::dataProducerBur,
            ConsentRequestCreatedDto::isCreated)
        .containsExactlyInAnyOrder(
            tuple(Uid.CHE102000001.name(), null, true),
            tuple(Uid.CHE102000001.name(), Bur.CODE_99920004.getCode(), true),
            tuple(Uid.CHE102000001.name(), Bur.CODE_99920006.getCode(), true),
            tuple(Uid.CHE102000002.name(), null, true),
            tuple(Uid.CHE102000002.name(), Bur.CODE_99920005.getCode(), true));

    assertThat(rowStates(Uid.CHE102000001)).containsOnly(OPENED);
    assertThat(rowStates(Uid.CHE102000002)).containsOnly(OPENED);
    assertThat(summaryState(Uid.CHE102000001)).isEqualTo(ConsentRequestAggregationStateEnum.OPENED);

    // Creating again is idempotent: nothing new, no duplicates.
    assertThat(createAcontrolConsents())
        .extracting(ConsentRequestCreatedDto::isCreated)
        .containsOnly(false);

    // PHASE 2 — update BUR states; the derived UID row is recomputed by the sync (GRANTED > DECLINED > OPENED).
    updateStatus(burRowId(Uid.CHE102000001, Bur.CODE_99920004), GRANTED, 204);
    updateStatus(burRowId(Uid.CHE102000001, Bur.CODE_99920006), DECLINED, 204);
    updateStatus(burRowId(Uid.CHE102000002, Bur.CODE_99920005), DECLINED, 204);

    // At least one BUR GRANTED -> UID roll-up GRANTED (intended precedence).
    assertThat(uidRollupState(Uid.CHE102000001)).isEqualTo(GRANTED);
    assertThat(burState(Uid.CHE102000001, Bur.CODE_99920004)).isEqualTo(GRANTED);
    assertThat(burState(Uid.CHE102000001, Bur.CODE_99920006)).isEqualTo(DECLINED);
    assertThat(summaryState(Uid.CHE102000001)).isEqualTo(ConsentRequestAggregationStateEnum.PARTIALLY_GRANTED);

    // Only BUR DECLINED -> UID roll-up DECLINED.
    assertThat(uidRollupState(Uid.CHE102000002)).isEqualTo(DECLINED);
    assertThat(summaryState(Uid.CHE102000002)).isEqualTo(ConsentRequestAggregationStateEnum.DECLINED);

    // PHASE 3 — forbidden: the UID row cannot be edited directly while active BUR rows exist.
    updateStatus(uidRowId(Uid.CHE102000001), GRANTED, 400);
    assertThat(uidRollupState(Uid.CHE102000001)).isEqualTo(GRANTED);

    // PHASE 4 — AGIS reports a deleted farm and an ownership change, so the cleanup job terminates two BUR relations.
    stubDeletedFarm(Bur.CODE_99920005, Uid.CHE102000002.name()); // CHE102000002's only BUR
    stubModifiedFarm(Bur.CODE_99920004, "CHE109999999"); // CHE102000001's GRANTED BUR changes owner
    cleanupJob.run();

    // CHE102000001: the only GRANTED BUR (99920004) is terminated; the sole remaining active BUR (99920006) is DECLINED
    // -> roll-up must follow down from GRANTED to DECLINED (this is the termination-path sync).
    assertThat(uidRollupState(Uid.CHE102000001)).isEqualTo(DECLINED);
    assertThat(burState(Uid.CHE102000001, Bur.CODE_99920006)).isEqualTo(DECLINED);

    // CHE102000002: no active BUR remains, so the roll-up is frozen at its last derived value (DECLINED).
    assertThat(uidRollupState(Uid.CHE102000002)).isEqualTo(DECLINED);
  }

  @Test
  void givenTwoBursOfSameUid_whenGrantedConcurrently_thenRollupIsGrantedWithoutLostUpdate() throws Exception {
    createAcontrolConsents();
    var bur4 = burRowId(Uid.CHE102000001, Bur.CODE_99920004);
    var bur6 = burRowId(Uid.CHE102000001, Bur.CODE_99920006);

    // Both BUR updates fire at the same instant (the client offers a single "grant all" button). Each update runs its own
    // transaction and writes the same UID roll-up row; they must serialize on the row lock rather than lose an update.
    var statusCodes = fireConcurrently(
        () -> putStatus(bur4, GRANTED),
        () -> putStatus(bur6, GRANTED));

    assertThat(statusCodes).containsOnly(204);
    assertThat(burState(Uid.CHE102000001, Bur.CODE_99920004)).isEqualTo(GRANTED);
    assertThat(burState(Uid.CHE102000001, Bur.CODE_99920006)).isEqualTo(GRANTED);
    assertThat(uidRollupState(Uid.CHE102000001)).isEqualTo(GRANTED);
  }

  // --- creation -------------------------------------------------------------------------------------------------------

  @SneakyThrows
  private List<ConsentRequestCreatedDto> createAcontrolConsents() {
    var createDtos = PRODUCER_B.getCompanyUids().stream()
        .map(uid -> CreateConsentRequestDto.builder().dataRequestId(ACONTROL_BIO_SUISSE.uuid()).uid(uid.name()).build())
        .toList();
    return AuthTestUtils.requestAs(PRODUCER_B).contentType(JSON)
        .body(MAPPER.writeValueAsString(createDtos))
        .when().post(ConsentRequestController.PATH)
        .then().statusCode(201)
        .extract().as(new TypeRef<>() {
        });
  }

  // --- status updates -------------------------------------------------------------------------------------------------

  private void updateStatus(UUID id, ConsentRequestStateEnum newStatus, int expectedStatusCode) {
    assertThat(putStatus(id, newStatus)).isEqualTo(expectedStatusCode);
  }

  private int putStatus(UUID id, ConsentRequestStateEnum newStatus) {
    return AuthTestUtils.requestAs(PRODUCER_B).contentType(JSON)
        .body(String.format("\"%s\"", newStatus))
        .when().put(ConsentRequestController.PATH + "/" + id + "/status")
        .then().extract().statusCode();
  }

  private List<Integer> fireConcurrently(Callable<Integer> first, Callable<Integer> second) throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(2);
    try {
      var startTogether = new CyclicBarrier(2);
      var firstResult = pool.submit(() -> {
        startTogether.await();
        return first.call();
      });
      var secondResult = pool.submit(() -> {
        startTogether.await();
        return second.call();
      });
      return List.of(firstResult.get(), secondResult.get());
    } finally {
      pool.shutdownNow();
    }
  }

  // --- reading through the aggregation endpoints ----------------------------------------------------------------------

  private ConsentRequestAggregationDto acontrolAggregation(Uid uid) {
    return AuthTestUtils.requestAs(PRODUCER_B)
        .when().get(ConsentRequestAggregationController.PATH + "/" + ACONTROL_BIO_SUISSE + "?dataProducerUid=" + uid)
        .then().statusCode(200)
        .extract().as(ConsentRequestAggregationDto.class);
  }

  private ConsentRequestAggregationStateEnum summaryState(Uid uid) {
    List<ConsentRequestAggregationSummaryDto> aggregations = AuthTestUtils.requestAs(PRODUCER_B)
        .when().get(ConsentRequestAggregationController.PATH + "?dataProducerUid=" + uid)
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });
    return aggregations.stream()
        .filter(aggregation -> aggregation.id().equals(ACONTROL_BIO_SUISSE.uuid()))
        .findFirst().orElseThrow()
        .stateCode();
  }

  private List<ConsentRequestStateEnum> rowStates(Uid uid) {
    return acontrolAggregation(uid).consentRequests().stream()
        .map(ConsentRequestProducerViewV2Dto::stateCode)
        .toList();
  }

  private ConsentRequestStateEnum uidRollupState(Uid uid) {
    return row(uid, consentRequest -> consentRequest.dataProducerBur() == null).stateCode();
  }

  private ConsentRequestStateEnum burState(Uid uid, Bur bur) {
    return row(uid, consentRequest -> bur.getCode().equals(consentRequest.dataProducerBur())).stateCode();
  }

  private UUID uidRowId(Uid uid) {
    return row(uid, consentRequest -> consentRequest.dataProducerBur() == null).id();
  }

  private UUID burRowId(Uid uid, Bur bur) {
    return row(uid, consentRequest -> bur.getCode().equals(consentRequest.dataProducerBur())).id();
  }

  private ConsentRequestProducerViewV2Dto row(Uid uid, Predicate<ConsentRequestProducerViewV2Dto> filter) {
    return acontrolAggregation(uid).consentRequests().stream()
        .filter(filter)
        .findFirst().orElseThrow();
  }

  // --- AGIS mutation stubs (override the defaults so only the test's own BURs get terminated) ------------------------

  private void stubDeletedFarm(Bur bur, String owningUid) {
    registerFarmMutation("deleted", bur.getCode(), owningUid);
  }

  private void stubModifiedFarm(Bur bur, String newOwningUid) {
    registerFarmMutation("modified", bur.getCode(), newOwningUid);
  }

  private void registerFarmMutation(String mutationType, String ber, String uid) {
    wireMock.register(WireMock.post(WireMock.urlEqualTo(AGIS_MUTATION_URL))
        .atPriority(1)
        .withRequestBody(WireMock.matchingJsonPath("$.mutationType", WireMock.equalTo(mutationType)))
        .withRequestBody(WireMock.matchingJsonPath("$.mutationDataType", WireMock.equalTo("farm")))
        .willReturn(WireMock.aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(farmTreeJson(ber, uid))));
  }

  private static String farmTreeJson(String ber, String uid) {
    return """
        {
          "personFarmTree": {
            "relevantPersons": { "person": [] },
            "personRelations": { "person": [] },
            "relevantFarms": { "farm": [ {"ktIdB": "", "agisIdB": "", "ber": "%s", "uid": "%s" } ] },
            "farmRelations": { "farm": [] }
          },
          "dataAmount": [],
          "resultOffset": { "from": 0, "to": 1, "totalHits": 1 }
        }
        """.formatted(ber, uid);
  }
}
