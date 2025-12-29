package integration.product;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;
import static integration.testutils.AccessTestUtils.HttpMethod.GET;

import ch.agridata.product.controller.DataProductController;
import integration.testutils.AccessTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AccessTest {

  @Test
  void testAccess() {
    AccessTestUtils.assertForbiddenForAllExcept(GET, DataProductController.PATH,
        PRODUCER_ROLE, CONSUMER_ROLE, ADMIN_ROLE, PROVIDER_ROLE, SUPPORT_ROLE);
  }

}
