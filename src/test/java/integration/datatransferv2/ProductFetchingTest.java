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
import java.time.LocalDate;
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
        // AGIS Data Products
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
            Map.of("uid", Uid.CHE102000002.name())),
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
        // TVD (Animal Tracing) Data Products
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_298B653C.uuid().toString(),
            Map.of("uid", Uid.CHE101000001.name(), "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_WITHOUT_UID,
            DataProduct.UUID_298B653C.uuid().toString(),
            Map.of("uid", Uid.CHE101000001.name(), "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_E08AF9D2.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_WITHOUT_UID,
            DataProduct.UUID_E08AF9D2.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_6319423C.uuid().toString(),
            Map.of("eartagNumber", "123456", "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_WITHOUT_UID,
            DataProduct.UUID_6319423C.uuid().toString(),
            Map.of("eartagNumber", "123456", "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_C4D3B0A3.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(),
                "dateFrom", LocalDate.now().toString(),
                "dateTo", LocalDate.now().toString(),
                "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_3E0BFD53.uuid().toString(),
            Map.of("ueln", "123456", "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_593913AC.uuid().toString(),
            Map.of("ueln", "123456", "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_B17AE68A.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_5DA9E6E0.uuid().toString(),
            Map.of("eartagNumber", "123456", "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_5AA2EE15.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(),
                "dateFrom", LocalDate.now().toString(),
                "dateTo", LocalDate.now().toString(),
                "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_7E4B1B3E.uuid().toString(),
            Map.of("eartagNumber", "123456", "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_C6F18E45.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(),
                "dateFrom", LocalDate.now().toString(),
                "dateTo", LocalDate.now().toString(),
                "recipientUid", "CHE123456789")),
        // ZO API Data Products
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_2F28D2EC.uuid().toString(),
            Map.of("dateFrom", LocalDate.now().toString(),
                "dateTo", LocalDate.now().toString(),
                "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_0B42AFB7.uuid().toString(),
            Map.of("eartagNumber", "123456", "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_D10B898F.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_59115547.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_88DCF0F9.uuid().toString(),
            Map.of("dateFrom", LocalDate.now().toString(),
                "dateTo", LocalDate.now().toString(),
                "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_54DECB00.uuid().toString(),
            Map.of("eartagNumber", "123456", "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_D156F252.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_720AA209.uuid().toString(),
            Map.of("dateFrom", LocalDate.now().toString(),
                "dateTo", LocalDate.now().toString(),
                "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_B0A4FF29.uuid().toString(),
            Map.of("eartagNumber", "123456", "recipientUid", "CHE123456789")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_E6128E10.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "recipientUid", "CHE123456789")),
        // Acontrol Data Products
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_30229210.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "pageSize", "5", "pageOffset", "0")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_96787125.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "pageSize", "5", "pageOffset", "0")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_DF72EB69.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "pageSize", "5", "pageOffset", "0")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_905170B2.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "pageSize", "5", "pageOffset", "0")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_3D3CB41C.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "pageSize", "5", "pageOffset", "0")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_75DB774F.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "pageSize", "5", "pageOffset", "0")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_1CA1904A.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "pageSize", "5", "pageOffset", "0")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_1F05FEF2.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "pageSize", "5", "pageOffset", "0")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_B993F539.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "pageSize", "5", "pageOffset", "0")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_C009A1F9.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "pageSize", "5", "pageOffset", "0")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_9F3A2E49.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "pageSize", "5", "pageOffset", "0")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_AF225782.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "pageSize", "5", "pageOffset", "0")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_54019A06.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "pageSize", "5", "pageOffset", "0")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_8791159F.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "pageSize", "5", "pageOffset", "0")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_340D517D.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "pageSize", "5", "pageOffset", "0")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_168AEC4A.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "pageSize", "5", "pageOffset", "0")),
        Arguments.of(
            TestUserEnum.CONSUMER_BLV_1,
            DataProduct.UUID_91682CE4.uuid().toString(),
            Map.of("bur", Bur.CODE_99910002.getCode(), "pageSize", "5", "pageOffset", "0"))
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
