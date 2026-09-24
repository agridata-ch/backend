package integration.agreement;

import static integration.testutils.TestDataIdentifiers.DataRequest.ACONTROL_BIO_SUISSE;
import static integration.testutils.TestDataIdentifiers.DataRequest.BIO_SUISSE_02;
import static integration.testutils.TestDataIdentifiers.DataRequest.BLV_ZO_CONSENT_FREE;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_1;
import static integration.testutils.TestUserEnum.CONSUMER_IP_SUISSE;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import ch.agridata.agreement.controller.DataRequestController;
import ch.agridata.agreement.dto.ConsentRequestCreatedDto;
import ch.agridata.agreement.dto.CreateConsentRequestsForUidDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.Bur;
import integration.testutils.TestDataIdentifiers.Uid;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ConsentRequestCreationForDataRequestConsumerTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * Start date of a direct farm-to-person relation in the AGIS stubs, see {@code BurAuthorizationTest}.
   */
  private static final LocalDateTime RELATION_SINCE = LocalDateTime.of(2025, 12, 11, 8, 23, 31);

  /**
   * Start date of a parent-child relation in the AGIS stubs, see {@code BurAuthorizationTest}.
   */
  private static final LocalDateTime PARENT_RELATION_SINCE = LocalDateTime.of(2025, 12, 11, 8, 23, 32);

  private final EntityManager entityManager;
  private final Flyway flyway;

  @BeforeEach
  void setUp() {
    flyway.migrate();
  }

  @Test
  void givenConsumer_whenCreateConsentRequestsWithBurs_thenUidAndBurRowsCreated() throws JsonProcessingException {
    var createdConsentRequests = postAsBioSuisse(ACONTROL_BIO_SUISSE.uuid(),
        CreateConsentRequestsForUidDto.builder()
            .uid(Uid.CHE102000001.name())
            .burs(List.of(Bur.CODE_99920004.getCode(), Bur.CODE_99920006.getCode()))
            .build())
        .then().statusCode(201)
        .extract().as(new TypeRef<List<ConsentRequestCreatedDto>>() {
        });

    assertThat(createdConsentRequests).hasSize(3)
        .extracting(ConsentRequestCreatedDto::dataProducerUid, ConsentRequestCreatedDto::dataProducerBur,
            ConsentRequestCreatedDto::isCreated)
        .containsExactlyInAnyOrder(
            tuple(Uid.CHE102000001.name(), null, true),
            tuple(Uid.CHE102000001.name(), Bur.CODE_99920004.getCode(), true),
            tuple(Uid.CHE102000001.name(), Bur.CODE_99920006.getCode(), true));

    assertThat(consentRequestRows(ACONTROL_BIO_SUISSE.uuid(), Uid.CHE102000001.name()))
        .extracting(row -> row[1], row -> row[2], row -> row[3])
        .containsExactlyInAnyOrder(
            tuple(null, null, null),
            tuple(Bur.CODE_99920004.getCode(), RELATION_SINCE, null),
            tuple(Bur.CODE_99920006.getCode(), PARENT_RELATION_SINCE, null));
  }

  @Test
  void givenConsumer_whenCreateConsentRequestsWithoutBurs_thenOnlyUidRowCreated() throws JsonProcessingException {
    var createdConsentRequests = postAsBioSuisse(ACONTROL_BIO_SUISSE.uuid(),
        CreateConsentRequestsForUidDto.builder().uid(Uid.CHE102000002.name()).burs(List.of()).build())
        .then().statusCode(201)
        .extract().as(new TypeRef<List<ConsentRequestCreatedDto>>() {
        });

    assertThat(createdConsentRequests).hasSize(1)
        .extracting(ConsentRequestCreatedDto::dataProducerUid, ConsentRequestCreatedDto::dataProducerBur,
            ConsentRequestCreatedDto::isCreated)
        .containsExactly(tuple(Uid.CHE102000002.name(), null, true));

    assertThat(consentRequestRows(ACONTROL_BIO_SUISSE.uuid(), Uid.CHE102000002.name()))
        .extracting(row -> row[1])
        .containsExactly((Object) null);
  }

  @Test
  void givenBursOmitted_whenCreateConsentRequests_thenBadRequestAndNothingCreated() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .contentType(JSON)
        .body("{\"uid\": \"" + Uid.CHE102000002.name() + "\"}")
        .when().post(consentRequestsUrl(ACONTROL_BIO_SUISSE.uuid()))
        .then().statusCode(400);

    assertThat(consentRequestRows(ACONTROL_BIO_SUISSE.uuid(), Uid.CHE102000002.name())).isEmpty();
  }

  @Test
  void givenDuplicateBurs_whenCreateConsentRequests_thenSingleBurRowCreated() throws JsonProcessingException {
    var createdConsentRequests = postAsBioSuisse(ACONTROL_BIO_SUISSE.uuid(),
        CreateConsentRequestsForUidDto.builder()
            .uid(Uid.CHE102000001.name())
            .burs(List.of(Bur.CODE_99920004.getCode(), Bur.CODE_99920004.getCode()))
            .build())
        .then().statusCode(201)
        .extract().as(new TypeRef<List<ConsentRequestCreatedDto>>() {
        });

    assertThat(createdConsentRequests).hasSize(2)
        .extracting(ConsentRequestCreatedDto::dataProducerUid, ConsentRequestCreatedDto::dataProducerBur,
            ConsentRequestCreatedDto::isCreated)
        .containsExactlyInAnyOrder(
            tuple(Uid.CHE102000001.name(), null, true),
            tuple(Uid.CHE102000001.name(), Bur.CODE_99920004.getCode(), true));

    assertThat(consentRequestRows(ACONTROL_BIO_SUISSE.uuid(), Uid.CHE102000001.name()))
        .filteredOn(row -> Bur.CODE_99920004.getCode().equals(row[1]))
        .hasSize(1);
  }

  @Test
  void givenExpiredBurConsentRequest_whenCreateConsentRequests_thenNewActiveRowIsAdded() throws JsonProcessingException {
    // Create a first BUR consent request and then expire it, so only a terminated row remains.
    postAsBioSuisse(ACONTROL_BIO_SUISSE.uuid(),
        CreateConsentRequestsForUidDto.builder().uid(Uid.CHE102000001.name()).burs(List.of(Bur.CODE_99920004.getCode())).build())
        .then().statusCode(201);
    expireBurConsentRequest(ACONTROL_BIO_SUISSE.uuid(), Uid.CHE102000001.name(), Bur.CODE_99920004.getCode());

    // The expired row must not block the request; a new active row is created next to it.
    postAsBioSuisse(ACONTROL_BIO_SUISSE.uuid(),
        CreateConsentRequestsForUidDto.builder().uid(Uid.CHE102000001.name()).burs(List.of(Bur.CODE_99920004.getCode())).build())
        .then().statusCode(201);

    assertThat(consentRequestRows(ACONTROL_BIO_SUISSE.uuid(), Uid.CHE102000001.name()))
        .filteredOn(row -> Bur.CODE_99920004.getCode().equals(row[1]))
        .extracting(row -> row[3])
        .containsExactlyInAnyOrder(null, LocalDateTime.of(2026, 1, 1, 0, 0));
  }

  @Test
  void givenActiveBurConsentRequest_whenCreateConsentRequestsForSameBur_thenBadRequestAndNothingCreated() throws JsonProcessingException {
    postAsBioSuisse(ACONTROL_BIO_SUISSE.uuid(),
        CreateConsentRequestsForUidDto.builder().uid(Uid.CHE102000001.name()).burs(List.of(Bur.CODE_99920004.getCode())).build())
        .then().statusCode(201);

    postAsBioSuisse(ACONTROL_BIO_SUISSE.uuid(),
        CreateConsentRequestsForUidDto.builder()
            .uid(Uid.CHE102000001.name())
            .burs(List.of(Bur.CODE_99920004.getCode(), Bur.CODE_99920006.getCode()))
            .build())
        .then().statusCode(400);

    // The whole request is rolled back: the second, previously missing BUR must not have been created.
    assertThat(consentRequestRows(ACONTROL_BIO_SUISSE.uuid(), Uid.CHE102000001.name()))
        .extracting(row -> row[1])
        .doesNotContain(Bur.CODE_99920006.getCode());
  }

  @Test
  void givenBurNotBelongingToUid_whenCreateConsentRequests_thenBadRequestAndNothingCreated() throws JsonProcessingException {
    postAsBioSuisse(ACONTROL_BIO_SUISSE.uuid(),
        CreateConsentRequestsForUidDto.builder().uid(Uid.CHE102000001.name()).burs(List.of("A00000000")).build())
        .then().statusCode(400);

    assertThat(consentRequestRows(ACONTROL_BIO_SUISSE.uuid(), Uid.CHE102000001.name())).isEmpty();
  }

  @Test
  void givenMixOfKnownAndUnknownBur_whenCreateConsentRequests_thenBadRequestAndNothingCreated() throws JsonProcessingException {
    // A99920004 belongs to the UID in the AGIS stubs, A00000000 does not.
    postAsBioSuisse(ACONTROL_BIO_SUISSE.uuid(),
        CreateConsentRequestsForUidDto.builder()
            .uid(Uid.CHE102000001.name())
            .burs(List.of(Bur.CODE_99920004.getCode(), "A00000000"))
            .build())
        .then().statusCode(400);

    // The BURs are validated before anything is persisted: neither the valid BUR nor the UID row must be created.
    assertThat(consentRequestRows(ACONTROL_BIO_SUISSE.uuid(), Uid.CHE102000001.name())).isEmpty();
  }

  @Test
  void givenMalformedBur_whenCreateConsentRequests_thenBadRequestAndNothingCreated() throws JsonProcessingException {
    postAsBioSuisse(ACONTROL_BIO_SUISSE.uuid(),
        CreateConsentRequestsForUidDto.builder().uid(Uid.CHE102000001.name()).burs(List.of("99920004")).build())
        .then().statusCode(400);

    assertThat(consentRequestRows(ACONTROL_BIO_SUISSE.uuid(), Uid.CHE102000001.name())).isEmpty();
  }

  @Test
  void givenDataRequestWithoutBurBasedProducts_whenCreateConsentRequestsWithBurs_thenBadRequestAndNothingCreated()
      throws JsonProcessingException {
    // BIO_SUISSE_02 only contains UID-based data products.
    postAsBioSuisse(BIO_SUISSE_02.uuid(),
        CreateConsentRequestsForUidDto.builder().uid(Uid.CHE101000001.name()).burs(List.of(Bur.CODE_99920004.getCode())).build())
        .then().statusCode(400);

    assertThat(consentRequestRows(BIO_SUISSE_02.uuid(), Uid.CHE101000001.name())).isEmpty();
  }

  @Test
  void givenNonActiveDataRequest_whenCreateConsentRequests_thenBadRequestAndNothingCreated() throws JsonProcessingException {
    QuarkusTransaction.requiringNew().run(() -> entityManager.createNativeQuery("""
            UPDATE data_request SET state_code = 'IN_REVIEW' WHERE id = :dataRequestId
            """)
        .setParameter("dataRequestId", ACONTROL_BIO_SUISSE.uuid())
        .executeUpdate());

    postAsBioSuisse(ACONTROL_BIO_SUISSE.uuid(),
        CreateConsentRequestsForUidDto.builder().uid(Uid.CHE102000001.name()).burs(List.of()).build())
        .then().statusCode(400);

    assertThat(consentRequestRows(ACONTROL_BIO_SUISSE.uuid(), Uid.CHE102000001.name())).isEmpty();
  }

  @Test
  void givenExistingUidConsentRequest_whenCreateConsentRequests_thenUidRowIsNotCreatedAgain() throws JsonProcessingException {
    // The test data contains a GRANTED UID consent request for CHE101000001 on ACONTROL_BIO_SUISSE.
    var createdConsentRequests = postAsBioSuisse(ACONTROL_BIO_SUISSE.uuid(),
        CreateConsentRequestsForUidDto.builder().uid(Uid.CHE101000001.name()).burs(List.of()).build())
        .then().statusCode(201)
        .extract().as(new TypeRef<List<ConsentRequestCreatedDto>>() {
        });

    assertThat(createdConsentRequests)
        .extracting(ConsentRequestCreatedDto::dataProducerUid, ConsentRequestCreatedDto::dataProducerBur,
            ConsentRequestCreatedDto::isCreated)
        .containsExactly(tuple(Uid.CHE101000001.name(), null, false));

    assertThat(consentRequestRows(ACONTROL_BIO_SUISSE.uuid(), Uid.CHE101000001.name()))
        .filteredOn(row -> row[1] == null)
        .hasSize(1);
  }

  @Test
  void givenDeclinedUidConsentRequest_whenCreateConsentRequestsWithBurs_thenUidStateIsSyncedWithBurs() throws JsonProcessingException {
    postAsBioSuisse(ACONTROL_BIO_SUISSE.uuid(),
        CreateConsentRequestsForUidDto.builder().uid(Uid.CHE102000001.name()).burs(List.of()).build())
        .then().statusCode(201);
    QuarkusTransaction.requiringNew().run(() -> entityManager.createNativeQuery("""
            UPDATE consent_request SET state_code = 'DECLINED'
            WHERE data_request_id = :dataRequestId AND data_producer_uid = :uid AND data_producer_bur IS NULL
            """)
        .setParameter("dataRequestId", ACONTROL_BIO_SUISSE.uuid())
        .setParameter("uid", Uid.CHE102000001.name())
        .executeUpdate());

    postAsBioSuisse(ACONTROL_BIO_SUISSE.uuid(),
        CreateConsentRequestsForUidDto.builder().uid(Uid.CHE102000001.name()).burs(List.of(Bur.CODE_99920004.getCode())).build())
        .then().statusCode(201);

    // The new BUR consent request is OPENED, so the UID consent request must follow it.
    assertThat(consentRequestStates(ACONTROL_BIO_SUISSE.uuid(), Uid.CHE102000001.name()))
        .extracting(row -> row[0], row -> row[1])
        .containsExactlyInAnyOrder(
            tuple(null, "OPENED"),
            tuple(Bur.CODE_99920004.getCode(), "OPENED"));
  }

  @Test
  void givenDataRequestWithoutConsentRequiredProducts_whenCreateConsentRequests_thenLegallyPermittedRowsCreated()
      throws JsonProcessingException {
    // All data products of BLV_ZO_CONSENT_FREE have consent_required = false.
    AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .contentType(JSON)
        .body(MAPPER.writeValueAsString(
            CreateConsentRequestsForUidDto.builder().uid(Uid.CHE102000001.name()).burs(List.of(Bur.CODE_99920004.getCode())).build()))
        .when().post(consentRequestsUrl(BLV_ZO_CONSENT_FREE.uuid()))
        .then().statusCode(201);

    assertThat(consentRequestStates(BLV_ZO_CONSENT_FREE.uuid(), Uid.CHE102000001.name()))
        .extracting(row -> row[0], row -> row[1])
        .containsExactlyInAnyOrder(
            tuple(null, "LEGALLY_PERMITTED"),
            tuple(Bur.CODE_99920004.getCode(), "LEGALLY_PERMITTED"));
  }

  @Test
  void givenDataRequestOfDifferentConsumer_whenCreateConsentRequests_thenNotFound() throws JsonProcessingException {
    // ACONTROL_BIO_SUISSE does not belong to the CONSUMER_IP_SUISSE test user.
    AuthTestUtils.requestAs(CONSUMER_IP_SUISSE)
        .contentType(JSON)
        .body(MAPPER.writeValueAsString(
            CreateConsentRequestsForUidDto.builder().uid(Uid.CHE102000001.name()).burs(List.of()).build()))
        .when().post(consentRequestsUrl(ACONTROL_BIO_SUISSE.uuid()))
        .then().statusCode(404);
  }

  @Test
  void givenDataRequestOfDifferentConsumer_whenCreateConsentRequestsWithBurs_thenNotFoundAndNothingCreated() throws JsonProcessingException {
    // Ownership is verified before AGIS: a non-owner posting BURs must get 404, not a BUR-validation error, and persist nothing.
    AuthTestUtils.requestAs(CONSUMER_IP_SUISSE)
        .contentType(JSON)
        .body(MAPPER.writeValueAsString(CreateConsentRequestsForUidDto.builder()
            .uid(Uid.CHE102000001.name())
            .burs(List.of(Bur.CODE_99920004.getCode()))
            .build()))
        .when().post(consentRequestsUrl(ACONTROL_BIO_SUISSE.uuid()))
        .then().statusCode(404);

    assertThat(consentRequestRows(ACONTROL_BIO_SUISSE.uuid(), Uid.CHE102000001.name())).isEmpty();
  }

  private Response postAsBioSuisse(UUID dataRequestId, CreateConsentRequestsForUidDto dto) throws JsonProcessingException {
    return AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .contentType(JSON)
        .body(MAPPER.writeValueAsString(dto))
        .when().post(consentRequestsUrl(dataRequestId));
  }

  private void expireBurConsentRequest(UUID dataRequestId, String uid, String bur) {
    QuarkusTransaction.requiringNew().run(() -> entityManager.createNativeQuery("""
            UPDATE consent_request SET uid_bur_relation_until = :terminatedAt
            WHERE data_request_id = :dataRequestId AND data_producer_uid = :uid AND data_producer_bur = :bur
            """)
        .setParameter("terminatedAt", LocalDateTime.of(2026, 1, 1, 0, 0))
        .setParameter("dataRequestId", dataRequestId)
        .setParameter("uid", uid)
        .setParameter("bur", bur)
        .executeUpdate());
  }

  @SuppressWarnings("unchecked")
  private List<Object[]> consentRequestRows(UUID dataRequestId, String uid) {
    return entityManager.createNativeQuery("""
            SELECT data_producer_uid, data_producer_bur, uid_bur_relation_since, uid_bur_relation_until
            FROM consent_request
            WHERE data_request_id = :dataRequestId AND archived = false AND data_producer_uid = :uid
            """)
        .setParameter("dataRequestId", dataRequestId)
        .setParameter("uid", uid)
        .getResultList();
  }

  @SuppressWarnings("unchecked")
  private List<Object[]> consentRequestStates(UUID dataRequestId, String uid) {
    return entityManager.createNativeQuery("""
            SELECT data_producer_bur, state_code
            FROM consent_request
            WHERE data_request_id = :dataRequestId AND archived = false AND data_producer_uid = :uid
            """)
        .setParameter("dataRequestId", dataRequestId)
        .setParameter("uid", uid)
        .getResultList();
  }

  private static String consentRequestsUrl(UUID dataRequestId) {
    return DataRequestController.PATH_V1 + "/" + dataRequestId + "/consent-requests";
  }
}
