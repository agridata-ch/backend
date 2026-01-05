package integration.user;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;
import static integration.testutils.AccessTestUtils.HttpMethod.GET;

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
        UserController.PATH + "/authorized-uids", PRODUCER_ROLE, SUPPORT_ROLE, ADMIN_ROLE);
    AccessTestUtils.assertForbiddenForAllExcept(GET,
        UserController.PATH + "/uid/1/authorized-burs", ADMIN_ROLE);
    AccessTestUtils.assertForbiddenForAllExcept(GET,
        UserController.PATH + "/producers", SUPPORT_ROLE);
  }

}
