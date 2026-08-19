package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;

import ch.agridata.agreement.dto.ConsentRequestAggregationDto;
import ch.agridata.agreement.dto.ConsentRequestAggregationSummaryDto;
import ch.agridata.agreement.mapper.ConsentRequestAggregationMapper;
import ch.agridata.agreement.mapper.DataRequestMapper;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.user.api.UserApi;
import ch.agridata.user.dto.UidDto;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * Provides read access to aggregated consent request information for data producers. It groups consent requests by their underlying
 * data request and delegates state and metadata derivation to {@link ConsentRequestAggregationMapper}.
 *
 * @CommentLastReviewed 2026-02-04
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ConsentRequestAggregationService {
  private final UserApi userApi;
  private final AgridataSecurityIdentity identity;
  private final ConsentRequestRepository consentRequestRepository;
  private final ConsentRequestAggregationMapper aggregationMapper;
  private final DataRequestMapper dataRequestMapper;
  private final DataRequestEnrichmentService dataRequestEnrichmentService;

  @RolesAllowed({PRODUCER_ROLE, SUPPORT_ROLE})
  public List<ConsentRequestAggregationSummaryDto> getConsentRequestAggregationsAsCurrentDataProducer(
      @NotNull String dataProducerUid
  ) {
    if (isNotAuthorized(dataProducerUid)) {
      return List.of();
    }

    var entities = consentRequestRepository.findActiveUidAndBurBasedByDataProducerUidsWithDataRequest(List.of(dataProducerUid));

    return entities.stream()
        .collect(Collectors.groupingBy(cr -> cr.getDataRequest().getId()))
        .values()
        .stream()
        .map(consentRequestEntities -> aggregationMapper.toConsentRequestAggregationSummaryDto(
            consentRequestEntities,
            dataRequestMapper.toDataRequestSummaryDto(consentRequestEntities.getFirst().getDataRequest())))
        .sorted(Comparator.comparing(
                ConsentRequestAggregationSummaryDto::requestDate,
                Comparator.nullsLast(Comparator.naturalOrder())
            )
            .reversed()
            .thenComparing(ConsentRequestAggregationSummaryDto::id))
        .toList();
  }

  @RolesAllowed({PRODUCER_ROLE, SUPPORT_ROLE})
  public ConsentRequestAggregationDto getConsentRequestAggregationAsCurrentDataProducer(
      @NotNull String dataProducerUid,
      @NotNull UUID dataRequestId
  ) {
    if (isNotAuthorized(dataProducerUid)) {
      throw new NotFoundException(dataRequestId.toString());
    }

    var group = consentRequestRepository.findActiveUidAndBurBasedByDataRequestIdAndDataProducerUid(dataRequestId, dataProducerUid);
    if (group.isEmpty()) {
      throw new NotFoundException(dataRequestId.toString());
    }

    var dataRequest = dataRequestEnrichmentService.toEnrichedDto(group.getFirst().getDataRequest());

    return aggregationMapper.toConsentRequestAggregationDto(group, dataRequest);
  }

  private boolean isNotAuthorized(String dataProducerUid) {
    var ktIdP = identity.getKtIdpOrImpersonatedKtIdP();
    var agateLoginId = identity.getAgateLoginIdOrImpersonatedAgateLoginId();

    var authorizedUids = userApi.getAuthorizedUids(ktIdP, agateLoginId)
        .stream()
        .map(UidDto::uid)
        .collect(Collectors.toSet());

    return !authorizedUids.contains(dataProducerUid);
  }
}
