package integration.testutils;

import static integration.testutils.TestDataIdentifiers.ConsentRequest.BIO_SUISSE_01_CHE101000001;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.SUPPORT;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.testdata.TestDataController;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class TestDataExecuteTest {

  private final EntityManager entityManager;

  @Test
  void givenNonAdmin_whenExecuteUpload_thenForbidden() {
    AuthTestUtils.requestAs(SUPPORT)
        .multiPart("file", "script.sql", "SELECT 1".getBytes(StandardCharsets.UTF_8), "application/sql")
        .when()
        .post(TestDataController.PATH + "/execute")
        .then()
        .statusCode(403);
  }

  @Test
  void givenMissingFile_whenExecuteUpload_thenBadRequest() {
    AuthTestUtils.requestAs(ADMIN)
        .contentType("multipart/form-data")
        .when()
        .post(TestDataController.PATH + "/execute")
        .then()
        .statusCode(400);
  }

  @Test
  void givenBlankSqlFile_whenExecuteUpload_thenBadRequest() {
    AuthTestUtils.requestAs(ADMIN)
        .multiPart("file", "script.sql", "   ".getBytes(StandardCharsets.UTF_8), "application/sql")
        .when()
        .post(TestDataController.PATH + "/execute")
        .then()
        .statusCode(400);
  }

  @Test
  void givenValidSqlFile_whenExecuteUpload_thenStatementIsApplied() {
    Boolean before =
        (Boolean)
            entityManager.createNativeQuery("SELECT archived FROM consent_request WHERE id = '" + BIO_SUISSE_01_CHE101000001 + "'::uuid")
                .getSingleResult();
    assertThat(before).isFalse();

    String sql = "UPDATE consent_request SET archived = true WHERE id = '" + BIO_SUISSE_01_CHE101000001 + "'";
    byte[] bytes = sql.getBytes(StandardCharsets.UTF_8);

    AuthTestUtils.requestAs(ADMIN)
        .multiPart("file", "script.sql", bytes, "application/sql")
        .when()
        .post(TestDataController.PATH + "/execute")
        .then()
        .statusCode(204);

    entityManager.clear();

    Boolean after =
        (Boolean)
            entityManager.createNativeQuery("SELECT archived FROM consent_request WHERE id = '" + BIO_SUISSE_01_CHE101000001 + "'::uuid")
                .getSingleResult();
    assertThat(after).isTrue();
  }
}
