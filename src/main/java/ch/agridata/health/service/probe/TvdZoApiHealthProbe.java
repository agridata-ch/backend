package ch.agridata.health.service.probe;

import ch.agridata.health.service.DataProviderProbe;
import ch.agridata.health.service.HealthException;
import ch.agridata.health.service.client.TvdZoApiHealthRestClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Health probe for the ZO-API data source system. Calls the ZO API's {@code api-zo/health} endpoint (authenticated via
 * the OIDC client of the REST client) and reports up on a 2xx response.
 *
 * @CommentLastReviewed 2026-10-21
 */
@ApplicationScoped
public class TvdZoApiHealthProbe implements DataProviderProbe {

  private final TvdZoApiHealthRestClient tvdZoApiHealthRestClient;

  @Inject
  public TvdZoApiHealthProbe(@RestClient TvdZoApiHealthRestClient tvdZoApiHealthRestClient) {
    this.tvdZoApiHealthRestClient = tvdZoApiHealthRestClient;
  }

  @Override
  public String dataSourceSystemCode() {
    return "ZO_API";
  }

  @Override
  public boolean isUp() {
    try (var response = tvdZoApiHealthRestClient.healthCheck()) {
      return response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL;
    } catch (HealthException _) {
      return false;
    }
  }
}
