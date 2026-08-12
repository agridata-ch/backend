package ch.agridata.agreement.mapper;

import ch.agridata.agreement.dto.ConsentRequestAggregationDto;
import ch.agridata.agreement.dto.ConsentRequestAggregationStateEnum;
import ch.agridata.agreement.dto.ConsentRequestAggregationSummaryDto;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.DataRequestSummaryDto;
import ch.agridata.agreement.persistence.ConsentRequestEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Assembles producer-facing consent request aggregations from a group of consent requests that share a data request. It derives the
 * aggregated state and metadata; the underlying data request DTO is supplied by the caller, since its enrichment is a service concern.
 *
 * @CommentLastReviewed 2026-08-13
 */

@Mapper(componentModel = "jakarta", uses = ConsentRequestMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ConsentRequestAggregationMapper {

  @Mapping(target = "id", source = "dataRequest.id")
  @Mapping(target = "stateCode", source = "consentRequests", qualifiedByName = "aggregateState")
  @Mapping(target = "requestDate", source = "consentRequests", qualifiedByName = "latestRequestDate")
  @Mapping(target = "showStateAsMigrated", source = "consentRequests", qualifiedByName = "anyShownAsMigrated")
  @Mapping(target = "lastStateChangeDate", source = "consentRequests", qualifiedByName = "latestStateChangeDate")
  @Mapping(target = "dataRequest", source = "dataRequest")
  @Mapping(target = "consentRequests", source = "consentRequests")
  ConsentRequestAggregationSummaryDto toConsentRequestAggregationSummaryDto(List<ConsentRequestEntity> consentRequests,
                                                                            DataRequestSummaryDto dataRequest);

  @Mapping(target = "id", source = "dataRequest.id")
  @Mapping(target = "stateCode", source = "consentRequests", qualifiedByName = "aggregateState")
  @Mapping(target = "requestDate", source = "consentRequests", qualifiedByName = "latestRequestDate")
  @Mapping(target = "showStateAsMigrated", source = "consentRequests", qualifiedByName = "anyShownAsMigrated")
  @Mapping(target = "lastStateChangeDate", source = "consentRequests", qualifiedByName = "latestStateChangeDate")
  @Mapping(target = "dataRequest", source = "dataRequest")
  @Mapping(target = "consentRequests", source = "consentRequests")
  ConsentRequestAggregationDto toConsentRequestAggregationDto(List<ConsentRequestEntity> consentRequests,
                                                              DataRequestDto dataRequest);

  @Named("anyShownAsMigrated")
  static boolean anyShownAsMigrated(List<ConsentRequestEntity> group) {
    return group.stream().anyMatch(ConsentRequestEntity::isShowStateAsMigrated);
  }

  @Named("latestRequestDate")
  static @Nullable LocalDate latestRequestDate(List<ConsentRequestEntity> group) {
    return group.stream()
        .map(ConsentRequestEntity::getRequestDate)
        .filter(Objects::nonNull)
        .map(LocalDateTime::toLocalDate)
        .max(LocalDate::compareTo)
        .orElse(null);
  }

  @Named("latestStateChangeDate")
  static @Nullable LocalDateTime latestStateChangeDate(List<ConsentRequestEntity> group) {
    return group.stream()
        .map(ConsentRequestEntity::getLastStateChangeDate)
        .filter(Objects::nonNull)
        .max(LocalDateTime::compareTo)
        .orElse(null);
  }

  @Named("aggregateState")
  static ConsentRequestAggregationStateEnum aggregateState(List<ConsentRequestEntity> group) {
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
