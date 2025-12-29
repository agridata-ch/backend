package integration.datatransfer;

import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static integration.testutils.AccessTestUtils.HttpMethod.GET;

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
        CONSUMER_ROLE);
    AccessTestUtils.assertForbiddenForAllExcept(GET, DataTransferController.PATH + "/product/1/delta",
        CONSUMER_ROLE);
  }
}
