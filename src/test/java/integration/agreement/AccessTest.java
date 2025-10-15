package integration.agreement;

import static integration.testutils.AccessTestUtils.HttpMethod.GET;
import static integration.testutils.AccessTestUtils.HttpMethod.POST;
import static integration.testutils.AccessTestUtils.HttpMethod.PUT;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.CONSUMER_IP_SUISSE;
import static integration.testutils.TestUserEnum.PRODUCER_032;
import static integration.testutils.TestUserEnum.SUPPORT;
import static io.restassured.http.ContentType.MULTIPART;

import ch.agridata.agreement.controller.ConsentRequestController;
import ch.agridata.agreement.controller.DataRequestController;
import integration.testutils.AccessTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class AccessTest {

  @Test
  void testAccess_ConsentRequestController() {
    AccessTestUtils.assertForbiddenForAllExcept(GET, ConsentRequestController.PATH,
        PRODUCER_032, SUPPORT);

    AccessTestUtils.assertForbiddenForAllExcept(PUT, ConsentRequestController.PATH + "/1/status",
        PRODUCER_032);
  }

  @Test
  void testAccess_DataRequestController() {
    AccessTestUtils.assertForbiddenForAllExcept(GET, DataRequestController.PATH,
        CONSUMER_BIO_SUISSE, CONSUMER_IP_SUISSE, ADMIN);

    AccessTestUtils.assertForbiddenForAllExcept(POST, DataRequestController.PATH,
        CONSUMER_BIO_SUISSE, CONSUMER_IP_SUISSE);

    AccessTestUtils.assertForbiddenForAllExcept(GET, DataRequestController.PATH + "/1",
        CONSUMER_BIO_SUISSE, CONSUMER_IP_SUISSE, ADMIN);

    AccessTestUtils.assertForbiddenForAllExcept(PUT, DataRequestController.PATH + "/1",
        CONSUMER_BIO_SUISSE, CONSUMER_IP_SUISSE);

    AccessTestUtils.assertForbiddenForAllExcept(PUT, DataRequestController.PATH + "/1/logo", MULTIPART,
        CONSUMER_BIO_SUISSE, CONSUMER_IP_SUISSE);

    AccessTestUtils.assertForbiddenForAllExcept(PUT, DataRequestController.PATH + "/1/status",
        CONSUMER_BIO_SUISSE, CONSUMER_IP_SUISSE, ADMIN);

    AccessTestUtils.assertForbiddenForAllExcept(GET, DataRequestController.PATH + "/1/kt-id-p/1/consent-requests",
        CONSUMER_BIO_SUISSE, CONSUMER_IP_SUISSE, ADMIN);

    AccessTestUtils.assertForbiddenForAllExcept(POST, ConsentRequestController.PATH + "/3da3a459-d3c2-48af-b8d0-02bc95146468/create",
        PRODUCER_032);
  }

}
