package integration.agreement;

import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.controller.DataRequestController;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import io.quarkus.test.junit.QuarkusTest;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ConsentRequestExportOfDataRequestTest {

  @Test
  void givenDataRequestOfCurrentConsumer_whenExporting_thenCsvContainsActiveConsentRequestsSortedByUidAndBur() {
    // BIO_SUISSE_01 also has a BUR-based consent request (A99910006) with a terminated UID/BUR relation, which must be excluded.
    var response = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(exportPath(TestDataIdentifiers.DataRequest.BIO_SUISSE_01))
        .then().statusCode(200)
        .contentType("text/csv")
        .header("Content-Disposition", "attachment; filename=\"consent-requests-" + TestDataIdentifiers.DataRequest.BIO_SUISSE_01
            + ".csv\"")
        .extract().asByteArray();

    assertThat(new String(response, StandardCharsets.UTF_8)).isEqualTo(
        "﻿"
            + "uid;bur;requestDate;lastChangeDate;stateCode\r\n"
            + "CHE101000001;;2025-03-14 10:12:33;2025-03-20 14:25:00;DECLINED\r\n"
            + "CHE101000001;A99910003;2025-03-14 10:12:33;2025-03-20 14:25:00;DECLINED\r\n"
            + "CHE102000001;;2025-02-11 16:48:20;;OPENED\r\n"
            + "CHE102000002;;2025-07-02 08:00:00;2025-07-03 08:00:00;GRANTED\r\n"
    );
  }

  @Test
  void givenDataRequestOfDifferentConsumer_whenExporting_thenNotFound() {
    // IP_SUISSE_01 does not belong to the CONSUMER_BIO_SUISSE test user.
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(exportPath(TestDataIdentifiers.DataRequest.IP_SUISSE_01))
        .then().statusCode(404);
  }

  private static String exportPath(TestDataIdentifiers.Identifier<?> dataRequestId) {
    return DataRequestController.PATH_V1 + "/" + dataRequestId + "/consent-requests/export";
  }
}
