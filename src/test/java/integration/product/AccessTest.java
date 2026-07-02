package integration.product;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;
import static integration.testutils.AccessTestUtils.HttpMethod.GET;
import static integration.testutils.AccessTestUtils.HttpMethod.POST;
import static integration.testutils.AccessTestUtils.HttpMethod.PUT;

import ch.agridata.product.controller.DataProductController;
import ch.agridata.product.controller.DataProductControllerV2;
import ch.agridata.product.controller.DataProviderController;
import integration.testutils.AccessTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AccessTest {

  @Test
  void testAccess_DataProductController() {
    AccessTestUtils.assertForbiddenForAllExcept(GET, DataProductController.PATH,
        PRODUCER_ROLE, CONSUMER_ROLE, ADMIN_ROLE, PROVIDER_ROLE, SUPPORT_ROLE);
  }

  @Test
  void testAccess_DataProductControllerV2() {
    AccessTestUtils.assertForbiddenForAllExcept(GET, DataProductControllerV2.PATH,
        ADMIN_ROLE, PROVIDER_ROLE);
    AccessTestUtils.assertForbiddenForAllExcept(GET, DataProductControllerV2.PATH + "/1",
        ADMIN_ROLE, PROVIDER_ROLE);
    AccessTestUtils.assertForbiddenForAllExcept(POST, DataProductControllerV2.PATH,
        ADMIN_ROLE, PROVIDER_ROLE);
    AccessTestUtils.assertForbiddenForAllExcept(PUT, DataProductControllerV2.PATH + "/1",
        ADMIN_ROLE, PROVIDER_ROLE);
    AccessTestUtils.assertForbiddenForAllExcept(PUT, DataProductControllerV2.PATH + "/1/status",
        ADMIN_ROLE, PROVIDER_ROLE);
  }

  @Test
  void testAccess_DataProviderController() {
    AccessTestUtils.assertForbiddenForAllExcept(GET, DataProviderController.PATH,
        PRODUCER_ROLE, CONSUMER_ROLE, ADMIN_ROLE, PROVIDER_ROLE, SUPPORT_ROLE);
    AccessTestUtils.assertForbiddenForAllExcept(GET, DataProviderController.PATH + "/1",
        PRODUCER_ROLE, CONSUMER_ROLE, ADMIN_ROLE, PROVIDER_ROLE, SUPPORT_ROLE);
    AccessTestUtils.assertForbiddenForAllExcept(GET, DataProviderController.PATH + "/1/products",
        PRODUCER_ROLE, CONSUMER_ROLE, ADMIN_ROLE, PROVIDER_ROLE, SUPPORT_ROLE);
    AccessTestUtils.assertForbiddenForAllExcept(GET, DataProviderController.PATH + "/1/dataSourceSystems",
        ADMIN_ROLE, PROVIDER_ROLE);
    AccessTestUtils.assertForbiddenForAllExcept(GET, DataProviderController.PATH + "/1/restClients",
        ADMIN_ROLE, PROVIDER_ROLE);
  }

}
