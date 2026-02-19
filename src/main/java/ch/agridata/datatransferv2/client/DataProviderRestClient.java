package ch.agridata.datatransferv2.client;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.core.Response;
import lombok.Builder;

/**
 * Defines the base contract for data provider clients. It standardizes post and get operations across providers.
 *
 * @CommentLastReviewed 2026-02-04
 */

public interface DataProviderRestClient {
  Response post(String path,
                Headers headers,
                Object body);

  Response get(String path,
               Headers headers);

  /**
   * Headers that are sent to the data provider
   *
   * @CommentLastReviewed 2026-02-04
   */
  @Builder
  class Headers {
    @HeaderParam("AGRIDATA-CONSUMER-UID")
    public String agridataConsumerUid;

    @HeaderParam("AGRIDATA-CONSUMER-AGATE-LOGIN-ID")
    public String agridataConsumerAgateLoginId;
  }
}
