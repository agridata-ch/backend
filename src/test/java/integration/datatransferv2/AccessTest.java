package integration.datatransferv2;

import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static integration.testutils.AccessTestUtils.HttpMethod.GET;

import ch.agridata.datatransferv2.controller.DataTransferController;
import integration.testutils.AccessTestUtils;
import integration.testutils.TestDataIdentifiers.DataProduct;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class AccessTest {

  @Test
  void testAccess() {
    AccessTestUtils.assertForbiddenForAllExcept(GET,
        DataTransferController.PATH + "/product/" + DataProduct.UUID_085E4B72 + "/data",
        CONSUMER_ROLE);

    AccessTestUtils.assertForbiddenForAllExcept(GET,
        DataTransferController.PATH + "/product/" + DataProduct.UUID_085E4B72 + "/modified-producers",
        CONSUMER_ROLE);
  }
}
