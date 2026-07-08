package integration.user;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;
import static integration.testutils.AccessTestUtils.HttpMethod.GET;
import static integration.testutils.AccessTestUtils.HttpMethod.POST;

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
    AccessTestUtils.assertForbiddenForAllExcept(POST,
        UserController.PATH + "/agb-revisions/73b2f303-2e87-4970-b82d-c5a6e296f1ec/accept",
        CONSUMER_ROLE, PROVIDER_ROLE);
  }

}
