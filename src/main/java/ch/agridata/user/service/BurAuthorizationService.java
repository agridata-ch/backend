package ch.agridata.user.service;

import ch.agridata.agis.api.AgisApi;
import ch.agridata.agis.dto.AgisFarmType;
import ch.agridata.agis.dto.AgisPersonFarmResponseType;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.user.dto.BurDto;
import ch.agridata.user.dto.BurParentLinkDto;
import ch.agridata.user.dto.FarmTypeEnum;
import ch.agridata.user.utils.AgisPersonFarmTreeUtils;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolves BURs authorized for a given UID by querying external register data. It maps register farms into BUR DTOs and determines the
 * start date of the UID-to-BUR relation, falling back to the parent farm when AGIS does not deliver a relation for a farm.
 *
 * @CommentLastReviewed 2026-08-10
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class BurAuthorizationService {

  private final AgisApi agisApi;

  public List<BurDto> getAuthorizedBurs(@NonNull String uid) {
    var personFarmResponse = agisApi.fetchRegisterDataForUid(uid);

    var personFarmTree = Optional.of(personFarmResponse)
        .map(AgisPersonFarmResponseType::getPersonFarmTree)
        .orElseThrow(() -> new ExternalWebServiceException("invalid response from AGIS: no personFarmTree found", null));

    var farms = AgisPersonFarmTreeUtils.getRelevantFarms(personFarmTree);
    var resolutionContext = new ResolutionContext(
        uid,
        AgisPersonFarmTreeUtils.indexFarmsByBer(farms),
        AgisPersonFarmTreeUtils.indexParentsByChildBer(farms),
        AgisPersonFarmTreeUtils.indexPersonToFarmValidSince(personFarmTree, uid)
    );

    return farms.stream()
        .map(farm -> mapToBurDto(farm, resolutionContext))
        .flatMap(Optional::stream)
        .toList();
  }

  private Optional<BurDto> mapToBurDto(@NonNull AgisFarmType farm, ResolutionContext context) {
    var relationSince = recursiveResolveRelationSince(farm, context, new HashSet<>());

    if (relationSince.isEmpty()) {
      log.error("no relation date could be determined for ber={} and uid={}, farm is skipped", farm.getBer(), context.targetUid());
      return Optional.empty();
    }

    return Optional.of(BurDto.builder()
        .uid(farm.getUid())
        .bur(farm.getBer())
        .farmTypeCode(FarmTypeEnum.fromNumber(farm.getFarmType()))
        .relationSince(relationSince.get())
        .build());
  }

  /**
   * Determines the start date of the relation between the target UID and the given farm. The three rules are checked in a fixed order and
   * the first match wins; an empty result means the farm cannot be attributed and must be skipped.
   * 1. Rule: Check for a farm-to-person relation in the data, take it's {@code validSince}.
   * 2. Rule: Check for a person-to-farm relation in the data, take it's {@code validSince}.
   * 3. Rule: Recursively repeat for the parent farm, and take the newer of the relationship-date and parents {@code validSince}.
   */
  private Optional<LocalDateTime> recursiveResolveRelationSince(AgisFarmType farm, ResolutionContext context, Set<String> visited) {
    if (!visited.add(farm.getBer())) { // Abort condition to protect from cyclic dependency
      return Optional.empty();
    }

    return findFarmToPersonDate(farm, context)
        .or(() -> findPersonToFarmDate(farm, context))
        .or(() -> findParentDate(farm, context, visited));
  }

  /**
   * Rule 1: Sometimes not set due to a current (2026) AGIS but, likely fix early 2027
   */
  private Optional<LocalDateTime> findFarmToPersonDate(AgisFarmType farm, ResolutionContext context) {
    return AgisPersonFarmTreeUtils.getFarmToPersonRelationValidSince(farm, context.targetUid());
  }

  /**
   * Rule 2
   */
  private Optional<LocalDateTime> findPersonToFarmDate(AgisFarmType farm, ResolutionContext context) {
    return Optional.ofNullable(context.personToFarmValidSince().get(farm.getBer()));
  }

  /**
   * Rule 3: Falls back to the parent farm: the result is the later of the parent relation date and the date resolved for the parent farm
   * itself.
   */
  private Optional<LocalDateTime> findParentDate(AgisFarmType farm, ResolutionContext context, Set<String> visited) {
    var parentLink = context.parentByChildBer().get(farm.getBer());
    var parentFarm = parentLink == null ? null : context.farmsByBer().get(parentLink.parentBer());

    if (parentFarm == null) {
      return Optional.empty();
    }

    return recursiveResolveRelationSince(parentFarm, context, visited)
        .map(
            parentDate -> parentDate.isAfter(parentLink.validSince())
                ? parentDate
                : parentLink.validSince()
        ); // Taking the newest date
  }

  /**
   * Holds the register data needed to resolve the relation date of a single farm.
   *
   * @CommentLastReviewed 2026-08-10
   */
  private record ResolutionContext(
      String targetUid,
      Map<String, AgisFarmType> farmsByBer,
      Map<String, BurParentLinkDto> parentByChildBer,
      Map<String, LocalDateTime> personToFarmValidSince
  ) {
  }
}
