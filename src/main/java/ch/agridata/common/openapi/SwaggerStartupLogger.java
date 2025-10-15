package ch.agridata.common.openapi;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Logs OpenAPI and Swagger initialization details. It provides visibility into API documentation setup.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Startup
@ApplicationScoped
public class SwaggerStartupLogger {

  @ConfigProperty(name = "quarkus.http.port", defaultValue = "8080")
  int port;

  @ConfigProperty(name = "quarkus.http.host", defaultValue = "localhost")
  String host;

  @ConfigProperty(name = "quarkus.swagger-ui.path", defaultValue = "/q/swagger-ui")
  String swaggerUiPath;

  @ConfigProperty(name = "quarkus.swagger-ui.enable", defaultValue = "true")
  boolean swaggerUiEnabled;

  @PostConstruct
  void onStartup() {
    if (!swaggerUiEnabled) {
      return;
    }

    String swaggerUrl = String.format("http://%s:%d%s", host, port, swaggerUiPath);
    System.out.println("✅ Swagger UI available at: " + swaggerUrl);
  }
}
