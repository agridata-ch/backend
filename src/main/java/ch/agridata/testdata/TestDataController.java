package ch.agridata.testdata;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.flywaydb.core.Flyway;

/**
 * Exposes endpoints for resetting test data. It validates the active environment profile and
 * executes Flyway migrations for full reset or per-user reset.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Path(TestDataController.PATH)
@Tag(name = "Test Data")
@RequiredArgsConstructor
@Slf4j
public class TestDataController {

  public static final String PATH = "/api/test-data";
  private static final String TEST_DATA_SCRIPT = "classpath:db/testdata/";
  private static final String USER_TEST_DATA_PATH = "classpath:db/testdatauser/";
  private static final List<String> ENABLED_PROFILES = List.of("local", "develop", "integration");
  private static final Set<String> VALID_UIDS = Set.of("CHE101000001", "CHE102000001",
      "CHE102000002", "CHE103000001", "CHE103000002");
  private final DataSource dataSource;

  @ConfigProperty(name = "quarkus.profile")
  String activeProfile;
  @ConfigProperty(name = "quarkus.flyway.locations")
  List<String> defaultFlywayLocations;

  @POST
  @Path("/reset")
  @Operation(operationId = "resetTestData")
  @Authenticated
  public void resetTestData() {
    validateProfile();
    log.info("test data reset initiated.");
    Flyway flyway = Flyway.configure().dataSource(dataSource).locations(
        Stream.concat(Stream.of(TEST_DATA_SCRIPT), defaultFlywayLocations.stream())
            .toArray(String[]::new)).load();

    flyway.migrate();

    log.info("test data reset completed.");
  }

  @POST
  @Path("/reset/user/{uid}")
  @Operation(operationId = "resetTestDataForUser", description =
      "Resets test data (consent requests) for a specific user UID without affecting other users' data. "
          + "Requires the full test data reset to have been run at least once to ensure data_requests exist.")
  @Authenticated
  public void resetTestDataForUser(@PathParam("uid") String uid) {
    validateProfile();
    validateUid(uid);

    log.info("test data reset initiated for user UID: {}", uid);

    String userScriptLocation = USER_TEST_DATA_PATH + uid;

    Flyway flyway = Flyway.configure().dataSource(dataSource).locations(
        Stream.concat(Stream.of(userScriptLocation), defaultFlywayLocations.stream())
            .toArray(String[]::new)).load();

    flyway.migrate();

    log.info("test data reset completed for user UID: {}", uid);
  }

  private void validateProfile() {
    if (!ENABLED_PROFILES.contains(activeProfile)) {
      throw new NotFoundException("Endpoint not available for profile: " + activeProfile);
    }
  }

  private void validateUid(String uid) {
    if (uid == null || uid.isBlank()) {
      throw new BadRequestException("UID must not be empty");
    }
    if (!VALID_UIDS.contains(uid)) {
      throw new NotFoundException("No test data available for UID: " + uid);
    }
  }
}
