package integration.user;

import static integration.testutils.AccessTestUtils.HttpMethod.GET;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.PRODUCER_032;
import static integration.testutils.TestUserEnum.SUPPORT;

import ch.agridata.user.controller.UserController;
import integration.testutils.AccessTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class AccessTest {

  @Test
  void testAccess() {
    AccessTestUtils.assertForbiddenForAllExcept(GET,
        UserController.PATH + "/authorized-uids", PRODUCER_032, SUPPORT);
    AccessTestUtils.assertForbiddenForAllExcept(GET,
        UserController.PATH + "/ktIdP/1/authorized-uids", ADMIN);
    AccessTestUtils.assertForbiddenForAllExcept(GET,
        UserController.PATH + "/uid/1/authorized-burs", ADMIN);
    AccessTestUtils.assertForbiddenForAllExcept(GET,
        UserController.PATH + "/producers", SUPPORT);
  }

}
