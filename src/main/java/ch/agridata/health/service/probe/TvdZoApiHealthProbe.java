package ch.agridata.health.service.probe;

import ch.agridata.health.service.DataProviderProbe;
import ch.agridata.health.service.HealthException;
import ch.agridata.health.service.client.TvdZoApiHealthRestClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Health probe for the ZO-API data source system. Calls the ZO API's {@code api-zo/health} endpoint with a fixed
 * {@code Authorization} token from configuration and reports up on a 2xx response.
 *
 * @CommentLastReviewed 2026-10-27
 */
@ApplicationScoped
public class TvdZoApiHealthProbe implements DataProviderProbe {

  private final TvdZoApiHealthRestClient tvdZoApiHealthRestClient;
  private final String authorizationToken;

  @Inject
  public TvdZoApiHealthProbe(
      @RestClient TvdZoApiHealthRestClient tvdZoApiHealthRestClient,
      @ConfigProperty(name = "agridata.tvd.health-check.authorization-token-zo-api") String authorizationToken) {
    this.tvdZoApiHealthRestClient = tvdZoApiHealthRestClient;
    this.authorizationToken = authorizationToken;
  }

  @Override
  public String dataSourceSystemCode() {
    return "ZO_API";
  }

  @Override
  public boolean isUp() {
    try (var response = tvdZoApiHealthRestClient.healthCheck(authorizationToken)) {
      return response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL;
    } catch (HealthException _) {
      return false;
    }
  }
}
