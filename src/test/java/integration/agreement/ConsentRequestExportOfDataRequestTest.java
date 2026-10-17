package integration.agreement;

import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.controller.DataRequestController;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.DataRequestStateEnum;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import io.quarkus.test.junit.QuarkusTest;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ConsentRequestExportOfDataRequestTest {

  private final Flyway flyway;
  private final Clock clock;

  @BeforeEach
  void setUp() {
    flyway.migrate();
  }

  @Test
  void givenDataRequestOfCurrentConsumer_whenExporting_thenCsvContainsActiveConsentRequestsSortedByUidAndBur() {
    // BIO_SUISSE_01 also has a BUR-based consent request (A99910006) with a terminated UID/BUR relation, which must be excluded.
    var response = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(exportPath(TestDataIdentifiers.DataRequest.BIO_SUISSE_01))
        .then().statusCode(200)
        .contentType("text/csv")
        .header("Content-Disposition", "attachment; filename=\"consent-requests-SV29-" + LocalDate.now(clock) + ".csv\"")
        .extract().asByteArray();

    assertThat(new String(response, StandardCharsets.UTF_8)).isEqualTo("\uFEFF" + """
        uid;bur;requestDate;lastChangeDate;stateCode\r
        CHE101000001;;2025-03-14 10:12:33;2025-03-20 14:25:00;DECLINED\r
        CHE101000001;A99910003;2025-03-14 10:12:33;2025-03-20 14:25:00;DECLINED\r
        CHE102000001;;2025-02-11 16:48:20;;OPENED\r
        CHE102000002;;2025-07-02 08:00:00;2025-07-03 08:00:00;GRANTED\r
        """);
  }

  @Test
  void givenDataRequestOfDifferentConsumer_whenExporting_thenNotFound() {
    // IP_SUISSE_01 does not belong to the CONSUMER_BIO_SUISSE test user.
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(exportPath(TestDataIdentifiers.DataRequest.IP_SUISSE_01))
        .then().statusCode(404);
  }

  @Test
  void givenAdmin_whenExportingNonDraftDataRequest_thenCsvIsReturned() {
    var response = AuthTestUtils.requestAs(ADMIN)
        .when().get(exportPath(TestDataIdentifiers.DataRequest.IP_SUISSE_01))
        .then().statusCode(200)
        .contentType("text/csv")
        .extract().asByteArray();

    assertThat(new String(response, StandardCharsets.UTF_8)).isEqualTo("\uFEFF" + """
        uid;bur;requestDate;lastChangeDate;stateCode\r
        CHE101000001;;2025-06-25 11:20:00;;OPENED\r
        CHE102000002;;2025-04-10 09:00:00;2025-04-13 11:30:00;GRANTED\r
        CHE103000001;;2025-01-30 10:10:10;2025-02-02 11:11:11;DECLINED\r
        CHE103000002;;2025-04-25 12:12:12;2025-04-30 17:00:00;DECLINED\r
        """);
  }

  @Test
  void givenAdmin_whenExportingDraftDataRequest_thenNotFound() {
    var draftId = DataRequestTestFactory.createDataRequest().as(DataRequestDto.class).id();

    AuthTestUtils.requestAs(ADMIN)
        .when().get(exportPath(draftId))
        .then().statusCode(404);
  }

  @Test
  void givenAdmin_whenExportingPausedDataRequest_thenCsvIsReturned() {
    DataRequestTestFactory.setStatusAs(TestDataIdentifiers.DataRequest.IP_SUISSE_01.toString(), DataRequestStateEnum.PAUSED, ADMIN)
        .then().statusCode(200);

    AuthTestUtils.requestAs(ADMIN)
        .when().get(exportPath(TestDataIdentifiers.DataRequest.IP_SUISSE_01))
        .then().statusCode(200)
        .contentType("text/csv");
  }

  @Test
  void givenDraftDataRequestOfCurrentConsumer_whenExporting_thenBadRequest() {
    var draftId = DataRequestTestFactory.createDataRequestAs(CONSUMER_BIO_SUISSE).as(DataRequestDto.class).id();

    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(exportPath(draftId))
        .then().statusCode(400);
  }

  @Test
  void givenPausedDataRequestOfCurrentConsumer_whenExporting_thenCsvIsReturned() {
    DataRequestTestFactory.setStatusAs(TestDataIdentifiers.DataRequest.BIO_SUISSE_01.toString(), DataRequestStateEnum.PAUSED, ADMIN)
        .then().statusCode(200);

    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(exportPath(TestDataIdentifiers.DataRequest.BIO_SUISSE_01))
        .then().statusCode(200)
        .contentType("text/csv");
  }

  private static String exportPath(TestDataIdentifiers.Identifier<?> dataRequestId) {
    return exportPath(dataRequestId.uuid());
  }

  private static String exportPath(UUID dataRequestId) {
    return DataRequestController.PATH_V1 + "/" + dataRequestId + "/consent-requests/export";
  }
}
