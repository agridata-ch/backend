package integration.user;

import static ch.agridata.auditing.api.ActionEnum.AGB_PUBLISHED;
import static ch.agridata.auditing.api.EntityTypeEnum.AGB_REVISION;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.CONSUMER_IP_SUISSE;
import static integration.testutils.TestUserEnum.PRODUCER_A;
import static integration.testutils.TestUserEnum.PROVIDER_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import ch.agridata.user.controller.AgbRevisionAdminController;
import ch.agridata.user.controller.UserController;
import integration.auditing.utils.AuditLogTestUtils;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.AgbRevision;
import integration.testutils.TestUserEnum;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Integration tests for the {@link AgbRevisionAdminController}.
 *
 * @CommentLastReviewed 2026-10-06
 */
@QuarkusTest
class AgbRevisionAdminControllerTest {

  private static final String PATH = AgbRevisionAdminController.PATH;
  private static final UUID INITIAL_CURRENT_REVISION_ID = AgbRevision.VERSION_1_4.uuid();
  private static final String UID_BIO_SUISSE = CONSUMER_BIO_SUISSE.getCompanyUids().getFirst().name();
  private static final String UID_IP_SUISSE = CONSUMER_IP_SUISSE.getCompanyUids().getFirst().name();
  private static final String UID_PROVIDER = PROVIDER_1.getCompanyUids().getFirst().name();

  @Inject
  Flyway flyway;

  @Inject
  EntityManager entityManager;

  @Inject
  AuditLogTestUtils auditLogTestUtils;

  @BeforeEach
  void setUp() {
    flyway.migrate();
  }

  // revisions are not part of the test data reset, so published revisions must be removed for the other tests
  @AfterEach
  void removePublishedRevisions() {
    QuarkusTransaction.requiringNew().run(() -> {
      entityManager.createNativeQuery("DELETE FROM agb_revision WHERE valid_from > (SELECT valid_from FROM agb_revision WHERE id = :id)")
          .setParameter("id", INITIAL_CURRENT_REVISION_ID)
          .executeUpdate();
      entityManager.createNativeQuery("UPDATE agb_revision SET valid_to = NULL WHERE id = :id")
          .setParameter("id", INITIAL_CURRENT_REVISION_ID)
          .executeUpdate();
    });
  }

  @Test
  void givenAdmin_whenGetAllAgbRevisions_thenReturnOverviewWithoutText() {
    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body("items", not(empty()))
        .body("items[0].id", notNullValue())
        .body("items[0].version", notNullValue())
        .body("items[0].validFrom", notNullValue())
        .body("items[0]", not(hasKey("agbText")));
  }

  @Test
  void givenAdmin_whenPageSizeOne_thenReturnSingleItemAndTotalCount() {
    var total = getItems(null, "version", 100).size();

    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .queryParam("page", 0)
        .queryParam("size", 1)
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body("items", hasSize(1))
        .body("totalItems", equalTo(total))
        .body("totalPages", equalTo(total));
  }

  @Test
  void givenAdmin_whenNoSortGiven_thenOrderedByValidFromDescending() {
    var validFrom = getItems(null, "validFrom", 100);

    assertThat(validFrom).isSortedAccordingTo(Comparator.reverseOrder());
  }

  @Test
  void givenAdmin_whenSortByVersionAscAndDesc_thenOrderIsReversed() {
    var asc = getItems("version", "version", 100);
    var desc = getItems("-version", "version", 100);

    assertThat(desc).containsExactlyElementsOf(asc.reversed());
  }

  @ParameterizedTest
  @ValueSource(strings = {"validTo", "-validTo", "enforceConsentFrom", "-enforceConsentFrom"})
  void givenAdmin_whenSortByOtherSortableField_thenAllRevisionsReturned(String sortBy) {
    var unsorted = getItems(null, "version", 100);

    assertThat(getItems(sortBy, "version", 100)).containsExactlyInAnyOrderElementsOf(unsorted);
  }

  @Test
  void givenAdmin_whenSortByUnknownField_thenBadRequest() {
    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .queryParam("sortBy", "agbText")
        .when()
        .get(PATH)
        .then()
        .statusCode(400);
  }

  @Test
  void givenExistingRevision_whenGetById_thenReturnDetailWithText() {
    var id = getItems(null, "id", 1).getFirst();

    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .when()
        .get(PATH + "/" + id)
        .then()
        .statusCode(200)
        .body("id", equalTo(id))
        .body("agbText.de", notNullValue())
        .body("validFrom", notNullValue());
  }

  @Test
  void givenUnknownId_whenGetById_thenNotFound() {
    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .when()
        .get(PATH + "/" + UUID.randomUUID())
        .then()
        .statusCode(404);
  }

  @Test
  void givenNewVersion_whenCreate_thenOldRevisionClosedNewOneActiveAndAudited() {
    var enforceFrom = LocalDateTime.now().plusDays(30).truncatedTo(ChronoUnit.SECONDS);
    var version = nextVersion();

    var id = AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .contentType(ContentType.JSON)
        .body(createBody(version, enforceFrom))
        .when()
        .post(PATH)
        .then()
        .statusCode(201)
        .body("version", equalTo(version))
        .body("validTo", nullValue())
        .body("validFrom", notNullValue())
        .extract()
        .path("id");

    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .when()
        .get(PATH + "/" + id)
        .then()
        .statusCode(200)
        .body("agbText.fr", equalTo("<p>fr</p>"));

    var openRevisions = AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .queryParam("size", 100)
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .extract()
        .jsonPath()
        .getList("items.findAll { it.validTo == null }.version", String.class);
    assertThat(openRevisions).containsExactly(version);

    var latest = auditLogTestUtils.getLatestAuditLogEntry();
    assertThat(latest.getActionCode()).isEqualTo(AGB_PUBLISHED.name());
    assertThat(latest.getEntityTypeCode()).isEqualTo(AGB_REVISION.name());
  }

  @Test
  void givenConsentRequired_whenCreate_thenEnforceDateBroughtForwardForConsumersAndProvidersOnly() {
    var enforceFrom = LocalDateTime.now().plusDays(30).truncatedTo(ChronoUnit.SECONDS);
    var earlierEnforceFrom = enforceFrom.minusDays(20);
    List.of(CONSUMER_BIO_SUISSE, CONSUMER_IP_SUISSE, PROVIDER_1, PRODUCER_A).forEach(this::createUserByFirstRequest);
    setEnforceAgbAcceptanceFrom(UID_BIO_SUISSE, null);
    setEnforceAgbAcceptanceFrom(UID_PROVIDER, enforceFrom.plusDays(60));
    setEnforceAgbAcceptanceFrom(UID_IP_SUISSE, earlierEnforceFrom);
    var producerEnforceFromBefore = getEnforceAgbAcceptanceFrom(PRODUCER_A);

    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .contentType(ContentType.JSON)
        .body(createBody(nextVersion(), enforceFrom))
        .when()
        .post(PATH)
        .then()
        .statusCode(201);

    assertThat(getEnforceAgbAcceptanceFrom(CONSUMER_BIO_SUISSE)).isEqualTo(enforceFrom);
    assertThat(getEnforceAgbAcceptanceFrom(PROVIDER_1)).isEqualTo(enforceFrom);
    assertThat(getEnforceAgbAcceptanceFrom(CONSUMER_IP_SUISSE)).isEqualTo(earlierEnforceFrom);
    assertThat(getEnforceAgbAcceptanceFrom(PRODUCER_A)).isEqualTo(producerEnforceFromBefore);
  }

  @Test
  void givenNoConsentRequired_whenCreate_thenEnforceDatesUnchanged() {
    createUserByFirstRequest(CONSUMER_BIO_SUISSE);
    setEnforceAgbAcceptanceFrom(UID_BIO_SUISSE, null);

    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .contentType(ContentType.JSON)
        .body(createBody(nextVersion(), null))
        .when()
        .post(PATH)
        .then()
        .statusCode(201);

    assertThat(getEnforceAgbAcceptanceFrom(CONSUMER_BIO_SUISSE)).isNull();
  }

  @Test
  void givenExistingVersion_whenCreate_thenConflictWithErrorCode() {
    var existingVersion = getItems(null, "version", 1).getFirst();

    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .contentType(ContentType.JSON)
        .body(createBody(existingVersion, null))
        .when()
        .post(PATH)
        .then()
        .statusCode(409)
        .body("type", equalTo("AGB_VERSION_ALREADY_EXISTS"));
  }

  @Test
  void givenVersionOlderThanCurrent_whenCreate_thenConflictWithErrorCode() {
    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .contentType(ContentType.JSON)
        .body(createBody("0.9", null))
        .when()
        .post(PATH)
        .then()
        .statusCode(409)
        .body("type", equalTo("AGB_VERSION_NOT_NEWER"));
  }

  @Test
  void givenNonNumericVersion_whenCreate_thenBadRequest() {
    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .contentType(ContentType.JSON)
        .body(createBody("abc", null))
        .when()
        .post(PATH)
        .then()
        .statusCode(400);
  }

  @Test
  void givenEnforceDateInThePast_whenCreate_thenBadRequest() {
    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .contentType(ContentType.JSON)
        .body(createBody(nextVersion(), LocalDateTime.now().minusDays(1)))
        .when()
        .post(PATH)
        .then()
        .statusCode(400);
  }

  @Test
  void givenBlankVersion_whenCreate_thenBadRequest() {
    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .contentType(ContentType.JSON)
        .body(createBody(" ", null))
        .when()
        .post(PATH)
        .then()
        .statusCode(400);
  }

  // the major version of the current revision plus one, which is always newer than the current revision
  private String nextVersion() {
    var currentVersion = AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .queryParam("size", 100)
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .extract()
        .jsonPath()
        .getString("items.find { it.validTo == null }.version");
    return (Integer.parseInt(currentVersion.split("\\.")[0]) + 1) + ".0";
  }

  // users are created on their first request
  private void createUserByFirstRequest(TestUserEnum user) {
    AuthTestUtils.requestAs(user).when().get(UserController.PATH + "/user-info").then().statusCode(200);
  }

  private LocalDateTime getEnforceAgbAcceptanceFrom(TestUserEnum user) {
    String enforceFrom = AuthTestUtils.requestAs(user)
        .when()
        .get(UserController.PATH + "/user-info")
        .then()
        .statusCode(200)
        .extract()
        .path("enforceAgbAcceptanceFrom");
    return enforceFrom == null ? null : LocalDateTime.parse(enforceFrom);
  }

  private void setEnforceAgbAcceptanceFrom(String uid, LocalDateTime enforceFrom) {
    QuarkusTransaction.requiringNew().run(
        () -> entityManager.createNativeQuery("""
                UPDATE users SET enforce_agb_acceptance_from = :enforceFrom WHERE uid = :uid
                """)
            .setParameter("enforceFrom", enforceFrom)
            .setParameter("uid", uid)
            .executeUpdate()
    );
  }

  private static Map<String, Object> createBody(String version, LocalDateTime enforceConsentFrom) {
    var body = new HashMap<String, Object>();
    body.put("version", version);
    body.put("agbText", Map.of("de", "<p>de</p>", "fr", "<p>fr</p>", "it", "<p>it</p>"));
    if (enforceConsentFrom != null) {
      body.put("enforceConsentFrom", enforceConsentFrom.toString());
    }
    return body;
  }

  private List<String> getItems(String sortBy, String field, int size) {
    var request = AuthTestUtils.requestAs(TestUserEnum.ADMIN).queryParam("size", size);
    if (sortBy != null) {
      request = request.queryParam("sortBy", sortBy);
    }
    return request
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .extract()
        .jsonPath()
        .getList("items." + field, String.class);
  }
}
