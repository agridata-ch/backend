package integration.agreement;

import static integration.testutils.TestDataIdentifiers.DataRequest.ACONTROL_BIO_SUISSE;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
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
  void givenDataRequestOfDifferentConsumer_whenCreateConsentRequests_thenNotFound() throws JsonProcessingException {
    // ACONTROL_BIO_SUISSE does not belong to the CONSUMER_IP_SUISSE test user.
    AuthTestUtils.requestAs(CONSUMER_IP_SUISSE)
        .contentType(JSON)
        .body(MAPPER.writeValueAsString(
            CreateConsentRequestsForUidDto.builder().uid(Uid.CHE102000001.name()).burs(List.of()).build()))
        .when().post(consentRequestsUrl(ACONTROL_BIO_SUISSE.uuid()))
        .then().statusCode(404);
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

  private static String consentRequestsUrl(UUID dataRequestId) {
    return DataRequestController.PATH_V1 + "/" + dataRequestId + "/consent-requests";
  }
}