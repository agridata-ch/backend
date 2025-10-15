package integration.product;

import static integration.testutils.AccessTestUtils.HttpMethod.GET;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.CONSUMER_IP_SUISSE;
import static integration.testutils.TestUserEnum.PRODUCER_032;
import static integration.testutils.TestUserEnum.PROVIDER;
import static integration.testutils.TestUserEnum.SUPPORT;

import ch.agridata.product.controller.DataProductController;
import integration.testutils.AccessTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AccessTest {

  @Test
  void testAccess() {
    AccessTestUtils.assertForbiddenForAllExcept(GET, DataProductController.PATH,
        PRODUCER_032, CONSUMER_BIO_SUISSE, CONSUMER_IP_SUISSE, ADMIN, PROVIDER, SUPPORT);
  }

}
