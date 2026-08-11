package ch.agridata.health.service.probe;

import ch.agridata.health.service.DataProviderProbe;
import ch.agridata.health.service.HealthException;
import ch.agridata.health.service.client.AgisApiRestClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Health probe for the AGIS (BLW) data source system. Reports up only when all AGIS subsystem {@code /health} endpoints
 * return 2xx via the authenticated AGIS client.
 *
 * @CommentLastReviewed 2026-08-11
 */
@ApplicationScoped
public class AgisApiHealthProbe implements DataProviderProbe {

  private static final List<String> HEALTH_PATHS = List.of(
      "register-data/2/health",
      "structure-data/2/health",
      "eco-etho-data/2/health");

  private final AgisApiRestClient agisApiRestClient;

  @Inject
  public AgisApiHealthProbe(@RestClient AgisApiRestClient agisApiRestClient) {
    this.agisApiRestClient = agisApiRestClient;
  }

  @Override
  public String dataSourceSystemCode() {
    return "AGIS";
  }

  @Override
  public boolean isUp() {
    return HEALTH_PATHS.stream().allMatch(this::isPathHealthy);
  }

  private boolean isPathHealthy(String path) {
    try (var response = agisApiRestClient.get(path)) {
      return response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL;
    } catch (HealthException _) {
      return false;
    }
  }
}
