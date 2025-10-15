package ch.agridata.common.actuators;

import static ch.agridata.common.actuators.InfoResource.PATH;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Provides an endpoint for retrieving system information. It enhances monitoring and debugging for running services.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Path(PATH)
@RequiredArgsConstructor
public class InfoResource {
  public static final String PATH = "/q/info";
  @ConfigProperty(name = "quarkus.application.name")
  String appName;

  @ConfigProperty(name = "quarkus.application.version", defaultValue = "dev")
  String version;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, String> info() {
    Map<String, String> info = new HashMap<>();
    info.put("appName", appName);
    info.put("version", version);
    return info;
  }

  @GET
  @Path("/schema")
  @Produces(MediaType.APPLICATION_JSON)
  public String schema() throws IOException {
    try (InputStream in = getClass().getClassLoader()
        .getResourceAsStream("schemas/agridata-schemas.json")) {
      if (in == null) {
        throw new WebApplicationException("Schema not found in classpath", 404);
      }
      return new String(in.readAllBytes());
    }
  }
}
