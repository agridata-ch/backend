package ch.agridata.tvd.service;

import ch.agridata.tvd.api.TvdApi;
import ch.agridata.tvd.dto.TvdEquidOwnerUidDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Provides the application-scoped implementation of {@link TvdApi}. Delegates TVD Animal Tracing API
 * calls to the configured REST client and exposes a simplified method that returns only the
 * response payload data for an AGATE login ID.
 *
 * @CommentLastReviewed 2025-12-29
 */

@ApplicationScoped
@Slf4j
public class TvdApiImpl implements TvdApi {

  private final TvdAnimalTracingApiRestClient tvdAnimalTracingApiRestClient;

  @Inject
  public TvdApiImpl(@RestClient TvdAnimalTracingApiRestClient tvdAnimalTracingApiRestClient) {
    this.tvdAnimalTracingApiRestClient = tvdAnimalTracingApiRestClient;
  }

  @Override
  public List<TvdEquidOwnerUidDto.Data> fetchEquidOwnerLegalUnits(@NonNull String agateLoginId) {
    return tvdAnimalTracingApiRestClient.fetchEquidOwnerLegalUnits(agateLoginId).data();
  }
}
