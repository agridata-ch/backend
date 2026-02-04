package integration.datatransferv2;

import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static integration.testutils.AccessTestUtils.HttpMethod.GET;

import ch.agridata.datatransferv2.controller.DataTransferController;
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
        DataTransferController.PATH + "/product/085e4b72-964d-4bd5-a3c9-224d8c5585af/data",
        CONSUMER_ROLE);
  }
}
