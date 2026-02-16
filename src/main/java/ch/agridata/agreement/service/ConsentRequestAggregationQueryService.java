package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;

import ch.agridata.agreement.dto.ConsentRequestAggregationProducerView;
import ch.agridata.agreement.dto.ConsentRequestAggregationStateEnum;
import ch.agridata.agreement.dto.ConsentRequestProducerViewDto;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.mapper.ConsentRequestMapper;
import ch.agridata.agreement.persistence.ConsentRequestEntity;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.user.api.UserApi;
import ch.agridata.user.dto.UidDto;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * Provides read access to aggregated consent request information for data producers. It groups consent requests by their underlying
 * data request and derives an aggregated state and metadata for each group.
 *
 * @CommentLastReviewed 2026-02-04
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ConsentRequestAggregationQueryService {
  private final UserApi userApi;
  private final AgridataSecurityIdentity identity;
  private final ConsentRequestRepository consentRequestRepository;
  private final ConsentRequestMapper consentRequestMapper;
  private final DataRequestEnrichmentService dataRequestEnrichmentService;

  @RolesAllowed({PRODUCER_ROLE, SUPPORT_ROLE})
  public List<ConsentRequestAggregationProducerView> getConsentRequestAggregationsAsCurrentDataProducer(@NotNull String dataProducerUid) {
    var ktIdP = identity.getKtIdpOrImpersonatedKtIdP();
    var agateLoginId = identity.getAgateLoginIdOrImpersonatedAgateLoginId();

    var authorizedUids = userApi.getAuthorizedUids(ktIdP, agateLoginId)
        .stream().map(UidDto::uid)
        .collect(Collectors.toSet());

    if (!authorizedUids.contains(dataProducerUid)) {
      return List.of();
    }

    var entities = consentRequestRepository.findByDataProducerUidsWithDataRequest(List.of(dataProducerUid));

    return entities.stream()
        .collect(Collectors.groupingBy(cr -> cr.getDataRequest().getId()))
        .entrySet().stream()
        .map(group -> toAggregate(group.getKey(), group.getValue()))
        .sorted(
            Comparator.comparing(
                ConsentRequestAggregationProducerView::requestDate,
                Comparator.nullsLast(Comparator.naturalOrder())
            ).reversed().thenComparing(ConsentRequestAggregationProducerView::id)
        )
        .toList();
  }

  private ConsentRequestAggregationProducerView toAggregate(UUID dataRequestId, List<ConsentRequestEntity> group) {
    if (group.isEmpty()) {
      throw new IllegalStateException("Group must not be empty for dataRequestId: " + dataRequestId);
    }

    var first = group.getFirst();

    ConsentRequestAggregationStateEnum state = aggregateState(group);

    LocalDateTime lastStateChangeDate =
        group.stream().map(ConsentRequestEntity::getLastStateChangeDate).filter(Objects::nonNull).max(LocalDateTime::compareTo)
            .orElse(null);

    DataRequestEntity dataRequest = first.getDataRequest();
    DataRequestDto dataRequestDto = dataRequestEnrichmentService.toEnrichedDto(dataRequest);

    List<ConsentRequestProducerViewDto> consentRequests = group.stream()
        .sorted(Comparator.comparing(ConsentRequestEntity::getId))
        .map(cr -> consentRequestMapper.toConsentRequestProducerViewDto(cr, dataRequestDto)).toList();

    var latestRequestDate =
        group.stream().map(ConsentRequestEntity::getRequestDate)
            .filter(Objects::nonNull).map(LocalDateTime::toLocalDate).max(LocalDate::compareTo).orElse(null);

    return ConsentRequestAggregationProducerView.builder()
        .id(dataRequestId)
        .dataProducerUid(first.getDataProducerUid())
        .stateCode(state)
        .lastStateChangeDate(lastStateChangeDate)
        .requestDate(latestRequestDate)
        .showStateAsMigrated(group.stream().anyMatch(ConsentRequestEntity::isShowStateAsMigrated))
        .dataRequest(dataRequestDto)
        .consentRequests(consentRequests)
        .build();
  }

  private static ConsentRequestAggregationStateEnum aggregateState(List<ConsentRequestEntity> group) {
    Map<ConsentRequestEntity.StateEnum, Long> counts =
        group.stream()
            .collect(Collectors.groupingBy(
                ConsentRequestEntity::getStateCode,
                () -> new EnumMap<>(ConsentRequestEntity.StateEnum.class),
                Collectors.counting()
            ));

    long total = group.size();
    long granted = counts.getOrDefault(ConsentRequestEntity.StateEnum.GRANTED, 0L);
    long opened = counts.getOrDefault(ConsentRequestEntity.StateEnum.OPENED, 0L);
    long declined = counts.getOrDefault(ConsentRequestEntity.StateEnum.DECLINED, 0L);

    if (granted == total) {
      return ConsentRequestAggregationStateEnum.GRANTED;
    }
    if (opened == total) {
      return ConsentRequestAggregationStateEnum.OPENED;
    }
    if (declined == total) {
      return ConsentRequestAggregationStateEnum.DECLINED;
    }

    if (opened > 0) {
      return ConsentRequestAggregationStateEnum.PARTIALLY_OPENED;
    }

    if (granted > 0 && declined > 0) {
      return ConsentRequestAggregationStateEnum.PARTIALLY_GRANTED;
    }

    throw new IllegalStateException("Unhandled state combination: " + counts);
  }
}
