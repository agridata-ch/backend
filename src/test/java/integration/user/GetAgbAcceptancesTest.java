package integration.user;

import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_1;
import static integration.testutils.TestUserEnum.CONSUMER_IP_SUISSE;
import static integration.testutils.TestUserEnum.PROVIDER_1;
import static integration.testutils.TestUserEnum.SUPPORT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.user.controller.AgbRevisionController;
import ch.agridata.user.controller.UserController;
import ch.agridata.user.dto.AgbAcceptanceOverviewDto;
import ch.agridata.user.dto.AgbAcceptanceStatusEnum;
import ch.agridata.user.dto.AgbRevisionDto;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestUserEnum;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the AGB acceptances endpoint of the {@link UserController}.
 *
 * @CommentLastReviewed 2026-10-22
 */
@QuarkusTest
@RequiredArgsConstructor
class GetAgbAcceptancesTest {

  private static final String PATH = UserController.PATH + "/agb-acceptances";
  private static final String UID_BIO_SUISSE = "CHE101708094";
  private static final String UID_IP_SUISSE = "CHE110013660";
  private static final String UID_PROVIDER = "CHE113614519";

  private final Flyway flyway;
  private final EntityManager entityManager;

  private AgbRevisionDto currentRevision;

  @BeforeEach
  void setUp() {
    flyway.migrate();

    // Users are created on their first request; BLV never accepts and must therefore not be listed
    List.of(CONSUMER_BIO_SUISSE, CONSUMER_IP_SUISSE, PROVIDER_1, CONSUMER_BLV_1).forEach(user ->
        AuthTestUtils.requestAs(user).when().get(UserController.PATH + "/user-info").then().statusCode(200));

    currentRevision = AuthTestUtils.requestAs(ADMIN)
        .when()
        .get(AgbRevisionController.PATH + "/current-agb-revision")
        .then()
        .statusCode(200)
        .extract()
        .as(AgbRevisionDto.class);

    List.of(CONSUMER_BIO_SUISSE, CONSUMER_IP_SUISSE, PROVIDER_1).forEach(user ->
        AuthTestUtils.requestAs(user)
            .when()
            .post(UserController.PATH + "/agb-revisions/" + currentRevision.id() + "/accept")
            .then()
            .statusCode(204));

    // A newer revision requires acceptance again: IP Suisse is still in the transition period, the provider's has passed
    setEnforceAgbAcceptanceFrom(UID_IP_SUISSE, LocalDateTime.now().plusDays(30));
    setEnforceAgbAcceptanceFrom(UID_PROVIDER, LocalDateTime.now().minusDays(1));
  }

  @Test
  void givenUsersInAllStates_whenGetAgbAcceptances_thenOnlyUsersWhoAcceptedReturnedWithStatus() {
    var items = getAgbAcceptances(ADMIN, Map.of());

    assertThat(items)
        .extracting(AgbAcceptanceOverviewDto::uid, AgbAcceptanceOverviewDto::status)
        .containsExactly(
            tuple(UID_BIO_SUISSE, AgbAcceptanceStatusEnum.ACCEPTED),
            tuple(UID_IP_SUISSE, AgbAcceptanceStatusEnum.TRANSITION_PERIOD),
            tuple(UID_PROVIDER, AgbAcceptanceStatusEnum.PENDING));
  }

  @Test
  void givenAcceptedUser_whenGetAgbAcceptances_thenLastAcceptedVersionAndDateReturned() {
    var accepted = getAgbAcceptances(SUPPORT, Map.of("searchTerm", UID_BIO_SUISSE)).getFirst();

    assertThat(accepted.lastAcceptedAgbVersion()).isEqualTo(currentRevision.version());
    assertThat(accepted.lastAcceptedAgbDate()).isNotNull();
    assertThat(accepted.givenName()).isEqualTo(CONSUMER_BIO_SUISSE.getGivenName());
  }

  @Test
  void givenPartialUid_whenGetAgbAcceptances_thenMatchedAsSubstring() {
    assertThat(getAgbAcceptances(ADMIN, Map.of("searchTerm", "che113614")))
        .extracting(AgbAcceptanceOverviewDto::uid)
        .containsExactly(UID_PROVIDER);
  }

  @Test
  void givenStatusFilter_whenGetAgbAcceptances_thenOnlyUsersWithStatusReturned() {
    assertThat(getAgbAcceptances(ADMIN, Map.of("filter", "status:PENDING")))
        .extracting(AgbAcceptanceOverviewDto::uid)
        .containsExactly(UID_PROVIDER);
    assertThat(getAgbAcceptances(ADMIN, Map.of("filter", "status:ACCEPTED,TRANSITION_PERIOD")))
        .extracting(AgbAcceptanceOverviewDto::uid)
        .containsExactly(UID_BIO_SUISSE, UID_IP_SUISSE);
  }

  @Test
  void givenNoSortBy_whenGetAgbAcceptances_thenSortedByUid() {
    // Updating the row moves it to the end of the table, so an unsorted query would no longer return uid order
    setEnforceAgbAcceptanceFrom(UID_BIO_SUISSE, null);

    assertThat(getAgbAcceptances(ADMIN, Map.of()))
        .extracting(AgbAcceptanceOverviewDto::uid)
        .containsExactly(UID_BIO_SUISSE, UID_IP_SUISSE, UID_PROVIDER);
  }

  @Test
  void givenSortByUidDescending_whenGetAgbAcceptances_thenOrderIsReversed() {
    assertThat(getAgbAcceptances(ADMIN, Map.of("sortBy", "-uid")))
        .extracting(AgbAcceptanceOverviewDto::uid)
        .containsExactly(UID_PROVIDER, UID_IP_SUISSE, UID_BIO_SUISSE);
  }

  @Test
  void givenUnknownStatus_whenGetAgbAcceptances_thenBadRequest() {
    AuthTestUtils.requestAs(ADMIN)
        .queryParam("filter", "status:UNKNOWN")
        .when()
        .get(PATH)
        .then()
        .statusCode(400);
  }

  @Test
  void givenUnknownFilterField_whenGetAgbAcceptances_thenBadRequest() {
    AuthTestUtils.requestAs(ADMIN)
        .queryParam("filter", "email:x")
        .when()
        .get(PATH)
        .then()
        .statusCode(400);
  }

  private void setEnforceAgbAcceptanceFrom(String uid, LocalDateTime enforceFrom) {
    QuarkusTransaction.requiringNew().run(() -> entityManager.createNativeQuery("""
            UPDATE users SET enforce_agb_acceptance_from = :enforceFrom WHERE uid = :uid
            """)
        .setParameter("enforceFrom", enforceFrom)
        .setParameter("uid", uid)
        .executeUpdate());
  }

  private List<AgbAcceptanceOverviewDto> getAgbAcceptances(TestUserEnum user, Map<String, String> queryParams) {
    return AuthTestUtils.requestAs(user)
        .queryParams(queryParams)
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .extract()
        .as(new TypeRef<PageResponseDto<AgbAcceptanceOverviewDto>>() {
        })
        .items();
  }
}
