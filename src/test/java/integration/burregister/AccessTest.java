package integration.burregister;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static integration.testutils.AccessTestUtils.HttpMethod.GET;

import ch.agridata.burregister.controller.BurRegisterController;
import integration.testutils.AccessTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AccessTest {

  @Test
  void testAccess() {
    AccessTestUtils.assertForbiddenForAllExcept(GET, BurRegisterController.PATH + "/search",
        CONSUMER_ROLE);

    AccessTestUtils.assertForbiddenForAllExcept(GET, BurRegisterController.PATH + "/search/CHE113614519",
        ADMIN_ROLE);
  }

}
