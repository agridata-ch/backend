package integration.datatransfer;

import static integration.testutils.AccessTestUtils.HttpMethod.GET;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.CONSUMER_IP_SUISSE;

import ch.agridata.datatransfer.controller.DataTransferController;
import integration.testutils.AccessTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class AccessTest {

  @Test
  void testAccess() {
    AccessTestUtils.assertForbiddenForAllExcept(GET, DataTransferController.PATH + "/product/1/data",
        CONSUMER_BIO_SUISSE, CONSUMER_IP_SUISSE);
    AccessTestUtils.assertForbiddenForAllExcept(GET, DataTransferController.PATH + "/product/1/delta",
        CONSUMER_BIO_SUISSE, CONSUMER_IP_SUISSE);
  }
}
