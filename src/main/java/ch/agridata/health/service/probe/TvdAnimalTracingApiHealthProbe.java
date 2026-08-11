package ch.agridata.health.service.probe;

import ch.agridata.health.service.DataProviderProbe;
import ch.agridata.health.service.HealthException;
import ch.agridata.health.service.client.TvdAnimalTracingApiRestClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Health probe for the Identitas Animal Tracing (TVD) data source system. Calls TVD's {@code /health-check} endpoint
 * with a fixed {@code Authorization} token from configuration and reports up on a 2xx response.
 *
 * @CommentLastReviewed 2026-08-11
 */
@ApplicationScoped
public class TvdAnimalTracingApiHealthProbe implements DataProviderProbe {

  private final TvdAnimalTracingApiRestClient tvdAnimalTracingApiRestClient;
  private final String authorizationToken;

  @Inject
  public TvdAnimalTracingApiHealthProbe(
      @RestClient TvdAnimalTracingApiRestClient tvdAnimalTracingApiRestClient,
      @ConfigProperty(name = "agridata.tvd.health-check.authorization-token") String authorizationToken) {
    this.tvdAnimalTracingApiRestClient = tvdAnimalTracingApiRestClient;
    this.authorizationToken = authorizationToken;
  }

  @Override
  public String dataSourceSystemCode() {
    return "TVD";
  }

  @Override
  public boolean isUp() {
    try (var response = tvdAnimalTracingApiRestClient.healthCheck(authorizationToken)) {
      return response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL;
    } catch (HealthException _) {
      return false;
    }
  }
}
