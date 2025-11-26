package ch.agridata.testdata;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Stream;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.flywaydb.core.Flyway;

/**
 * Exposes an endpoint for resetting test data. It validates the active environment profile, executes Flyway migrations against the test
 * data scripts, and provides responses indicating success or failure.
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
  private static final List<String> ENABLED_PROFILES = List.of("local", "develop", "integration");
  private final DataSource dataSource;

  @ConfigProperty(name = "quarkus.profile")
  String activeProfile;
  @ConfigProperty(name = "quarkus.flyway.locations")
  List<String> defaultFlywayLocations;

  @POST
  @Path("/reset")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(operationId = "resetTestData")
  @Authenticated
  public void resetTestData() {
    if (!ENABLED_PROFILES.contains(activeProfile)) {
      throw new NotFoundException(activeProfile);
    }
    log.info("test data reset initiated.");
    Flyway flyway = Flyway.configure()
        .dataSource(dataSource)
        .locations(
            Stream.concat(
                Stream.of(TEST_DATA_SCRIPT),
                defaultFlywayLocations.stream()
            ).toArray(String[]::new))
        .load();

    flyway.migrate();
    log.info("test data reset completed.");
  }
}
