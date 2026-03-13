package integration.datatransferv2;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.datatransferv2.controller.DataTransferController;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.Bur;
import integration.testutils.TestDataIdentifiers.DataProduct;
import integration.testutils.TestDataIdentifiers.Uid;
import integration.testutils.TestUserEnum;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Map;
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
            Map.of("uid", Uid.CHE102000002.name())),
        Arguments.of(
            TestUserEnum.CONSUMER_BIO_SUISSE,
            DataProduct.UUID_147E8C40.uuid().toString(),
            Map.of("uid", Uid.CHE102000002.name())),
        Arguments.of(
            TestUserEnum.CONSUMER_BIO_SUISSE,
            DataProduct.UUID_7911D98D.uuid().toString(),
            Map.of("uid", Uid.CHE102000002.name())
        ),
        Arguments.of(
            TestUserEnum.CONSUMER_BIO_SUISSE,
            DataProduct.UUID_46F8A883.uuid().toString(),
            Map.of("uid", Uid.CHE102000002.name(), "year", "2024")),
        Arguments.of(
            TestUserEnum.CONSUMER_BIO_SUISSE,
            DataProduct.UUID_085E4B72.uuid().toString(),
            Map.of("uid", Uid.CHE103000001.name(), "year", "2024")),
        Arguments.of(
            TestUserEnum.CONSUMER_BIO_SUISSE,
            DataProduct.UUID_A795D0B0.uuid().toString(),
            Map.of("uid", Uid.CHE103000001.name(), "year", "2024")),
        Arguments.of(
            TestUserEnum.CONSUMER_IP_SUISSE,
            DataProduct.UUID_0A808700.uuid().toString(),
            Map.of("uid", Uid.CHE101000001.name(), "year", "2024")),
        Arguments.of(
            TestUserEnum.CONSUMER_IP_SUISSE,
            DataProduct.UUID_EF4F42DD.uuid().toString(),
            Map.of("uid", Uid.CHE101000001.name(), "year", "2024")),
        Arguments.of(
            TestUserEnum.CONSUMER_IP_SUISSE,
            DataProduct.UUID_2375219C.uuid().toString(),
            Map.of("uid", Uid.CHE101000001.name(), "year", "2024")),
        Arguments.of(
            TestUserEnum.CONSUMER_IP_SUISSE,
            DataProduct.UUID_1DAD9F91.uuid().toString(),
            Map.of("uid", Uid.CHE101000001.name(), "year", "2024")),
        Arguments.of(
            TestUserEnum.CONSUMER_IP_SUISSE,
            DataProduct.UUID_64E39DF0.uuid().toString(),
            Map.of("uid", Uid.CHE102000002.name(), "year", "2024")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_298B653C.uuid().toString(),
            Map.of("uid", Uid.ZZZ199984051.name(), "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_WITHOUT_UID,
            DataProduct.UUID_298B653C.uuid().toString(),
            Map.of("uid", Uid.ZZZ199984051.name(), "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_E08AF9D2.uuid().toString(),
            Map.of("bur", Bur._99910002.getCode(), "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_WITHOUT_UID,
            DataProduct.UUID_E08AF9D2.uuid().toString(),
            Map.of("bur", Bur._99910002.getCode(), "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_6319423C.uuid().toString(),
            Map.of("eartagNumber", "123456", "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_WITHOUT_UID,
            DataProduct.UUID_6319423C.uuid().toString(),
            Map.of("eartagNumber", "123456", "recipientUid", "CHE123456789"))
    );
  }

  @ParameterizedTest(name = "[Product: {1}] consumer={0}, queryParams={2}")
  @MethodSource("productFetchingTestSource")
  void testCorrectProductFetching(TestUserEnum requestAs,
                                  String requestedProductId,
                                  Map<String, String> queryParams) {
    var request = AuthTestUtils.requestAs(requestAs)
        .pathParam("productId", requestedProductId);
    queryParams.forEach(request::queryParam);
    request
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then().statusCode(200)
        .header("AGRIDATA-REQUEST-ID", notNullValue())
        .body("dataOfProduct", equalTo(requestedProductId));
  }
}
