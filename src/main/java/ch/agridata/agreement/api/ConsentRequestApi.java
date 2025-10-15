package ch.agridata.agreement.api;

import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Defines the API interface for managing consent requests. It specifies the operations available to external clients.
 *
 * @CommentLastReviewed 2025-10-02
 */
public interface ConsentRequestApi {
  @RolesAllowed(CONSUMER_ROLE)
  List<UUID> getConsentRequestIdsOfCurrentConsumerGrantedByProducerForProductByBur(@Valid @NotNull String bur,
                                                                                   @Valid @NotNull UUID productId);

  @RolesAllowed(CONSUMER_ROLE)
  List<UUID> getConsentRequestIdsOfCurrentConsumerGrantedByProducerForProductByUid(@Valid @NotNull String uid,
                                                                                   @Valid @NotNull UUID productId);

  @RolesAllowed(CONSUMER_ROLE)
  List<String> getGrantedConsentRequestUidsForProductOfCurrentConsumerSince(@Valid @NotNull UUID productId,
                                                                            @Valid @NotNull LocalDateTime since);

}
