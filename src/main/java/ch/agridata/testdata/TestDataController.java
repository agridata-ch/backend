package ch.agridata.testdata;

import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;

import ch.agridata.common.openapi.ApiSubset;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Stream;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.flywaydb.core.Flyway;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

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
  private static final List<String> ENABLED_PROFILES = List.of("local", "test", "develop", "testing");
  private final DataSource dataSource;

  @ConfigProperty(name = "quarkus.profile")
  String activeProfile;
  @ConfigProperty(name = "quarkus.flyway.locations")
  List<String> defaultFlywayLocations;

  @POST
  @ApiSubset({WEB_APP})
  @Path("/execute")
  @Operation(operationId = "executeSqlScript")
  @Transactional
  @RolesAllowed({ADMIN_ROLE})
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public void execute(@RestForm("file") FileUpload file) throws SQLException, IOException {
    if (!ENABLED_PROFILES.contains(activeProfile)) {
      throw new NotFoundException(activeProfile);
    }
    if (file == null || file.size() == 0) {
      throw new ValidationException("file must not be null/empty");
    }

    String filename = file.fileName();
    if (filename == null || !filename.toLowerCase().endsWith(".sql")) {
      throw new ValidationException("file must have .sql extension");
    }

    log.info("SQL file execution initiated: {}", filename);

    String sql;
    try (InputStream in = file.uploadedFile().toUri().toURL().openStream()) {
      sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    if (sql.isBlank()) {
      throw new ValidationException("sql must not be null/empty");
    }

    try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
      s.execute(sql);
    }

    log.info("SQL file execution completed: {}", filename);
  }

  @POST
  @ApiSubset({WEB_APP})
  @Path("/reset")
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
