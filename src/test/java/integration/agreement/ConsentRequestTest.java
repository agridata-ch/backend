package integration.agreement;

import static integration.testutils.TestDataIdentifiers.ConsentRequest.IP_SUISSE_01_CHE101000001;
import static integration.testutils.TestDataIdentifiers.ConsentRequest.IP_SUISSE_01_CHE102000002;
import static integration.testutils.TestDataIdentifiers.DataRequest.ACONTROL_BIO_SUISSE;
import static integration.testutils.TestUserEnum.PRODUCER_B;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.equalTo;

import ch.agridata.agreement.controller.ConsentRequestController;
import ch.agridata.agreement.dto.ConsentRequestCreatedDto;
import ch.agridata.agreement.dto.CreateConsentRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import integration.testutils.TestDataIdentifiers.Bur;
import integration.testutils.TestDataIdentifiers.Uid;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.quarkus.narayana.jta.QuarkusTransaction;
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
class ConsentRequestTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** Start date of a direct farm-to-person relation in the AGIS stubs, see {@code BurAuthorizationTest}. */
  private static final LocalDateTime RELATION_SINCE = LocalDateTime.of(2025, 12, 11, 8, 23, 31);

  /** Start date of a parent-child relation in the AGIS stubs, see {@code BurAuthorizationTest}. */
  private static final LocalDateTime PARENT_RELATION_SINCE = LocalDateTime.of(2025, 12, 11, 8, 23, 32);

  private final EntityManager entityManager;
  private final Flyway flyway;

  @BeforeEach
  void setUp() {
    flyway.migrate();
  }

  @Test
  void givenProducer_whenGetConsentRequest_thenConsentRequestReturned() {
    AuthTestUtils.requestAs(PRODUCER_B)
        .when().get(ConsentRequestController.PATH + "/" + IP_SUISSE_01_CHE102000002)
        .then().statusCode(200)
        .body("id", equalTo(IP_SUISSE_01_CHE102000002.toString()))
        .extract().as(new TypeRef<>() {
        });
  }

  @Test
  void givenProducer_whenGetConsentRequestOfDifferentUser_thenShouldReturnNotFound() {
    AuthTestUtils.requestAs(PRODUCER_B)
        .when().get(ConsentRequestController.PATH + "/" + IP_SUISSE_01_CHE101000001)
        .then().statusCode(404);

  }

  @Test
  void givenProducer_whenConsentRequestAreRequestedWithoutExplicitUid_thenAllConsentRequestsReturned() {
    AuthTestUtils.requestAs(PRODUCER_B)
        .when().get(ConsentRequestController.PATH)
        .then().statusCode(200)
        .body("size()", equalTo(7))
        .extract().as(new TypeRef<>() {
        });
  }

  @Test
  void givenProducer_whenConsentRequestAreRequestedWithExplicitUid_thenFilteredConsentRequestsReturned() {
    AuthTestUtils.requestAs(PRODUCER_B)
        .when().get(ConsentRequestController.PATH + "?dataProducerUid=" + Uid.CHE102000001)
        .then().statusCode(200)
        .body("size()", equalTo(3))
        .extract().as(new TypeRef<>() {
        });
  }

  @Test
  void givenProducer_whenConsentRequestAreRequestedWithUncorrelatedUid_thenNoConsentRequestsReturned() {
    AuthTestUtils.requestAs(PRODUCER_B)
        .when().get(ConsentRequestController.PATH + "?dataProducerUid=" + Uid.CHE101000001)
        .then().statusCode(200)
        .body("size()", equalTo(0))
        .extract().as(new TypeRef<>() {
        });
  }

  @Test
  void givenProducer_whenCreateConsentRequests_thenCreatedConsentRequestReturned() throws JsonProcessingException {
    var createDtos = PRODUCER_B.getCompanyUids().stream()
        .map(uid -> CreateConsentRequestDto.builder().dataRequestId(TestDataIdentifiers.DataRequest.BIO_SUISSE_01.uuid()).uid(uid.name())
            .build())
        .toList();
    List<ConsentRequestCreatedDto> createdConsentRequests = AuthTestUtils.requestAs(PRODUCER_B)
        .contentType(JSON)
        .body(MAPPER.writeValueAsString(createDtos))
        .when().post(ConsentRequestController.PATH)
        .then().statusCode(201)
        .extract().as(new TypeRef<>() {
        });
    assertThat(createdConsentRequests).hasSize(2).extracting(ConsentRequestCreatedDto::dataProducerUid)
        .containsExactlyInAnyOrderElementsOf(PRODUCER_B.getCompanyUids().stream().map(Uid::name).toList());

    // BIO_SUISSE_01 has UID based data products only, so no BUR consent request must be created
    assertThat(consentRequestRowsOfProducerB(TestDataIdentifiers.DataRequest.BIO_SUISSE_01.uuid()))
        .hasSize(2)
        .allSatisfy(row -> assertThat(row[1]).isNull());
  }

  @Test
  void givenDataRequestWithBurProducts_whenCreateConsentRequests_thenBurConsentRequestsAreCreatedAsWell() throws JsonProcessingException {
    var createdConsentRequests = createConsentRequestsForAcontrolDataRequest();

    // The response stays on UID level, one entry per submitted DTO
    assertThat(createdConsentRequests).hasSize(2)
        .extracting(ConsentRequestCreatedDto::dataProducerUid, ConsentRequestCreatedDto::isCreated)
        .containsExactlyInAnyOrder(
            tuple(Uid.CHE102000001.name(), true),
            tuple(Uid.CHE102000002.name(), true));

    assertThat(consentRequestRowsOfProducerB(ACONTROL_BIO_SUISSE.uuid()))
        .extracting(row -> row[0], row -> row[1], row -> row[2], row -> row[3])
        .containsExactlyInAnyOrder(
            tuple(Uid.CHE102000001.name(), null, null, null),
            tuple(Uid.CHE102000001.name(), Bur.CODE_99920004.getCode(), RELATION_SINCE, null),
            tuple(Uid.CHE102000001.name(), Bur.CODE_99920006.getCode(), PARENT_RELATION_SINCE, null),
            tuple(Uid.CHE102000002.name(), null, null, null),
            tuple(Uid.CHE102000002.name(), Bur.CODE_99920005.getCode(), RELATION_SINCE, null));
  }

  @Test
  void givenExistingConsentRequests_whenCreateConsentRequestsAgain_thenNothingIsDuplicated() throws JsonProcessingException {
    createConsentRequestsForAcontrolDataRequest();

    var createdConsentRequests = createConsentRequestsForAcontrolDataRequest();

    assertThat(createdConsentRequests).hasSize(2)
        .extracting(ConsentRequestCreatedDto::isCreated)
        .containsOnly(false);
    assertThat(consentRequestRowsOfProducerB(ACONTROL_BIO_SUISSE.uuid())).hasSize(5);
  }

  @Test
  void givenTerminatedBurRelation_whenCreateConsentRequestsAgain_thenNewActiveRowIsCreatedAndTerminatedRowStaysUntouched()
      throws JsonProcessingException {
    createConsentRequestsForAcontrolDataRequest();

    // The farm's relation to CHE102000001/99920004 ended, e.g. because ownership changed away and back again.
    var terminatedAt = LocalDateTime.of(2026, 1, 1, 0, 0);
    QuarkusTransaction.requiringNew().run(() -> entityManager.createNativeQuery("""
            UPDATE consent_request SET uid_bur_relation_until = :terminatedAt
            WHERE data_request_id = :dataRequestId AND data_producer_uid = :uid AND data_producer_bur = :bur
            """)
        .setParameter("terminatedAt", terminatedAt)
        .setParameter("dataRequestId", ACONTROL_BIO_SUISSE.uuid())
        .setParameter("uid", Uid.CHE102000001.name())
        .setParameter("bur", Bur.CODE_99920004.getCode())
        .executeUpdate());

    // Creating consent requests again must not fail on the unique index and must not touch the terminated row.
    createConsentRequestsForAcontrolDataRequest();

    assertThat(consentRequestRowsOfProducerB(ACONTROL_BIO_SUISSE.uuid()))
        .filteredOn(row -> Uid.CHE102000001.name().equals(row[0]) && Bur.CODE_99920004.getCode().equals(row[1]))
        .extracting(row -> row[2], row -> row[3])
        .containsExactlyInAnyOrder(
            tuple(RELATION_SINCE, terminatedAt),
            tuple(RELATION_SINCE, null));
  }

  @Test
  void givenUnauthorizedUid_whenCreateConsentRequests_thenNoConsentRequestIsCreated() throws JsonProcessingException {
    postConsentRequests(List.of(
        CreateConsentRequestDto.builder().dataRequestId(ACONTROL_BIO_SUISSE.uuid()).uid(Uid.CHE102000001.name()).build(),
        CreateConsentRequestDto.builder().dataRequestId(ACONTROL_BIO_SUISSE.uuid()).uid(Uid.CHE101000001.name()).build()))
        .then().statusCode(400);

    // The whole batch is rolled back, the authorized UID must not be persisted either
    assertThat(consentRequestRowsOfProducerB(ACONTROL_BIO_SUISSE.uuid())).isEmpty();
  }

  @Test
  void givenUnknownDataRequest_whenCreateConsentRequests_thenNotFound() throws JsonProcessingException {
    postConsentRequests(List.of(CreateConsentRequestDto.builder()
        .dataRequestId(TestDataIdentifiers.DataRequest.BIO_SUISSE_DRAFT.uuid())
        .uid(Uid.CHE102000001.name())
        .build()))
        .then().statusCode(404);
  }

  private List<ConsentRequestCreatedDto> createConsentRequestsForAcontrolDataRequest() throws JsonProcessingException {
    var createDtos = PRODUCER_B.getCompanyUids().stream()
        .map(uid -> CreateConsentRequestDto.builder().dataRequestId(ACONTROL_BIO_SUISSE.uuid()).uid(uid.name()).build())
        .toList();

    return postConsentRequests(createDtos)
        .then().statusCode(201)
        .extract().as(new TypeRef<>() {
        });
  }

  private Response postConsentRequests(List<CreateConsentRequestDto> createDtos) throws JsonProcessingException {
    return AuthTestUtils.requestAs(PRODUCER_B)
        .contentType(JSON)
        .body(MAPPER.writeValueAsString(createDtos))
        .when().post(ConsentRequestController.PATH);
  }

  @SuppressWarnings("unchecked")
  private List<Object[]> consentRequestRowsOfProducerB(UUID dataRequestId) {
    return entityManager.createNativeQuery("""
            SELECT data_producer_uid, data_producer_bur, uid_bur_relation_since, uid_bur_relation_until
            FROM consent_request
            WHERE data_request_id = :dataRequestId AND archived = false AND data_producer_uid IN :dataProducerUids
            """)
        .setParameter("dataRequestId", dataRequestId)
        .setParameter("dataProducerUids", PRODUCER_B.getCompanyUids().stream().map(Uid::name).toList())
        .getResultList();
  }
}
