package integration.datatransfer;

import static ch.agridata.agreement.dto.ConsentRequestStateEnum.GRANTED;
import static integration.testutils.TestDataIdentifiers.Uid.CHE102000001;
import static integration.testutils.TestDataIdentifiers.Uid.CHE102000002;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.PRODUCER_032;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.controller.ConsentRequestController;
import ch.agridata.datatransfer.controller.DataTransferController;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class DeltaTest {

  @Test
  void givenGrantedConsents_whenDeltaRequested_thenIdsReturned() {
    LocalDateTime testStartTime = LocalDateTime.now();

    assertThat(getDeltaIds(testStartTime)).isEmpty();

    AuthTestUtils.requestAs(PRODUCER_032).contentType(ContentType.JSON)
        .body(String.format("\"%s\"", GRANTED)).when()
        .put(ConsentRequestController.PATH + "/" + TestDataIdentifiers.ConsentRequest.BIO_SUISSE_02_CHE102000001 + "/status")
        .then().statusCode(204);

    AuthTestUtils.requestAs(PRODUCER_032).contentType(ContentType.JSON)
        .body(String.format("\"%s\"", GRANTED)).when()
        .put(ConsentRequestController.PATH + "/" + TestDataIdentifiers.ConsentRequest.BIO_SUISSE_02_CHE102000002 + "/status")
        .then().statusCode(204);

    assertThat(getDeltaIds(testStartTime)).containsExactlyInAnyOrderElementsOf(List.of(CHE102000001.name(), CHE102000002.name()));
  }

  private List<String> getDeltaIds(LocalDateTime since) {
    return AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", TestDataIdentifiers.DataProduct.UUID_085E4B72.uuid())
        .queryParam("since", since.toString())
        .when().get(DataTransferController.PATH + "/product/{productId}/delta")
        .then().statusCode(200)
        .extract()
        .as(new TypeRef<>() {
        });
  }

}
