package integration.datatransferv2;

import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.datatransferv2.controller.DataTransferController;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.DataProduct;
import integration.testutils.TestDataIdentifiers.Uid;
import integration.testutils.TestUserEnum;
import io.quarkus.test.junit.QuarkusTest;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@RequiredArgsConstructor
class ProductFetchingTest {

  static Stream<Arguments> productFetchingTestSource() {
    return Stream.of(
        Arguments.of(
            TestUserEnum.CONSUMER_BIO_SUISSE,
            DataProduct.UUID_C661EA48.uuid().toString(),
            Uid.CHE102000002.name()),
        Arguments.of(
            TestUserEnum.CONSUMER_BIO_SUISSE,
            DataProduct.UUID_147E8C40.uuid().toString(),
            Uid.CHE102000002.name()),
        Arguments.of(
            TestUserEnum.CONSUMER_BIO_SUISSE,
            DataProduct.UUID_46F8A883.uuid().toString(),
            Uid.CHE102000002.name()),
        Arguments.of(
            TestUserEnum.CONSUMER_BIO_SUISSE,
            DataProduct.UUID_085E4B72.uuid().toString(),
            Uid.CHE103000001.name()),
        Arguments.of(
            TestUserEnum.CONSUMER_BIO_SUISSE,
            DataProduct.UUID_A795D0B0.uuid().toString(),
            Uid.CHE103000001.name()),
        Arguments.of(
            TestUserEnum.CONSUMER_IP_SUISSE,
            DataProduct.UUID_0A808700.uuid().toString(),
            Uid.CHE101000001.name()),
        Arguments.of(
            TestUserEnum.CONSUMER_IP_SUISSE,
            DataProduct.UUID_EF4F42DD.uuid().toString(),
            Uid.CHE101000001.name()),
        Arguments.of(
            TestUserEnum.CONSUMER_IP_SUISSE,
            DataProduct.UUID_2375219C.uuid().toString(),
            Uid.CHE101000001.name()),
        Arguments.of(
            TestUserEnum.CONSUMER_IP_SUISSE,
            DataProduct.UUID_1DAD9F91.uuid().toString(),
            Uid.CHE101000001.name()),
        Arguments.of(
            TestUserEnum.CONSUMER_IP_SUISSE,
            DataProduct.UUID_64E39DF0.uuid().toString(),
            Uid.CHE102000002.name()),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_298B653C.uuid().toString(),
            Uid.ZZZ199984051.name()),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_WITHOUT_UID,
            DataProduct.UUID_298B653C.uuid().toString(),
            Uid.ZZZ199984051.name())
    );
  }

  @ParameterizedTest(name = "[{0}] uid={2}, productId={1}")
  @MethodSource("productFetchingTestSource")
  void testCorrectProductFetching(TestUserEnum requestAs,
                                  String requestedProductId,
                                  String requestedUid) {
    AuthTestUtils.requestAs(requestAs)
        .pathParam("productId", requestedProductId)
        .queryParam("uid", requestedUid)
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then().statusCode(200)
        .header("AGRIDATA-REQUEST-ID", notNullValue())
        .body(notNullValue());
  }
}
