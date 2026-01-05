package integration.agreement;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;
import static integration.testutils.AccessTestUtils.HttpMethod.GET;
import static integration.testutils.AccessTestUtils.HttpMethod.POST;
import static integration.testutils.AccessTestUtils.HttpMethod.PUT;
import static integration.testutils.TestDataIdentifiers.ConsentRequest.IP_SUISSE_01_CHE102000002;
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
        PRODUCER_ROLE, SUPPORT_ROLE);

    AccessTestUtils.assertForbiddenForAllExcept(PUT, ConsentRequestController.PATH + "/1/status",
        PRODUCER_ROLE);
  }

  @Test
  void testAccess_DataRequestController() {
    AccessTestUtils.assertForbiddenForAllExcept(GET, DataRequestController.PATH,
        CONSUMER_ROLE, ADMIN_ROLE);

    AccessTestUtils.assertForbiddenForAllExcept(POST, DataRequestController.PATH,
        CONSUMER_ROLE);

    AccessTestUtils.assertForbiddenForAllExcept(GET, DataRequestController.PATH + "/1",
        CONSUMER_ROLE, ADMIN_ROLE);

    AccessTestUtils.assertForbiddenForAllExcept(PUT, DataRequestController.PATH + "/1",
        CONSUMER_ROLE);

    AccessTestUtils.assertForbiddenForAllExcept(GET, DataRequestController.PATH + "/1/consent-requests",
        ADMIN_ROLE);

    AccessTestUtils.assertForbiddenForAllExcept(PUT, DataRequestController.PATH + "/1/logo", MULTIPART,
        CONSUMER_ROLE);

    AccessTestUtils.assertForbiddenForAllExcept(PUT, DataRequestController.PATH + "/1/status",
        CONSUMER_ROLE, ADMIN_ROLE);

    AccessTestUtils.assertForbiddenForAllExcept(GET, DataRequestController.PATH + "/1/kt-id-p/1/consent-requests",
        CONSUMER_ROLE, ADMIN_ROLE);

    AccessTestUtils.assertForbiddenForAllExcept(POST, ConsentRequestController.PATH,
        PRODUCER_ROLE);

    AccessTestUtils.assertForbiddenForAllExcept(GET, ConsentRequestController.PATH + "/" + IP_SUISSE_01_CHE102000002,
        PRODUCER_ROLE, SUPPORT_ROLE);
  }

}
