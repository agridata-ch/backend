package ch.agridata.user.utils;

import ch.agridata.agis.dto.AgisFarmChildRelations;
import ch.agridata.agis.dto.AgisFarmParentRelations;
import ch.agridata.agis.dto.AgisFarmToPersonRelations;
import ch.agridata.agis.dto.AgisFarmType;
import ch.agridata.agis.dto.AgisKtIdBRelationType;
import ch.agridata.agis.dto.AgisKtIdPRelationType;
import ch.agridata.agis.dto.AgisPersonFarmTreeType;
import ch.agridata.agis.dto.AgisPersonToFarmRelations;
import ch.agridata.agis.dto.AgisPersonType;
import ch.agridata.agis.dto.AgisRelevantFarms;
import ch.agridata.user.dto.BurParentLinkDto;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Utils for navigating {@link AgisPersonFarmTreeType} and {@link AgisFarmType}
 *
 * @CommentLastReviewed 2026-08-10
 */
public class AgisPersonFarmTreeUtils {

  private AgisPersonFarmTreeUtils() {
  }

  public static Map<String, AgisFarmType> indexFarmsByBer(List<AgisFarmType> farms) {
    var farmsByBer = new LinkedHashMap<String, AgisFarmType>();
    farms.forEach(farm -> {
      if (farm.getBer() != null) {
        farmsByBer.putIfAbsent(farm.getBer(), farm);
      }
    });
    return farmsByBer;
  }

  /**
   * Indexes the parent farm per child BUR. AGIS expresses the same parent-child edge in two ways, either as a parent relation on the child
   * or as a child relation on the parent, so both directions are collected here.
   */
  public static Map<String, BurParentLinkDto> indexParentsByChildBer(List<AgisFarmType> farms) {
    var parentByChildBer = new HashMap<String, BurParentLinkDto>();
    farms.forEach(farm -> {
      getFarmParentRelations(farm).forEach(relation -> putParentLink(parentByChildBer, farm.getBer(), relation.getBer(), relation));
      getFarmChildRelations(farm).forEach(relation -> putParentLink(parentByChildBer, relation.getBer(), farm.getBer(), relation));
    });
    return parentByChildBer;
  }

  private static void putParentLink(
      Map<String, BurParentLinkDto> parentByChildBer, String childBer, String parentBer,
      AgisKtIdBRelationType relation
  ) {
    if (childBer != null && parentBer != null && relation.getValidSince() != null) {
      parentByChildBer.put(childBer, new BurParentLinkDto(parentBer, relation.getValidSince().toLocalDateTime()));
    }
  }

  public static Map<String, LocalDateTime> indexPersonToFarmValidSince(AgisPersonType person) {
    var validSinceByBer = new HashMap<String, LocalDateTime>();

    Optional.ofNullable(person.getPersonToFarmRelations())
        .map(AgisPersonToFarmRelations::getPersonToFarmRelation)
        .stream()
        .flatMap(List::stream)
        .forEach(relation -> {
          if (relation.getBer() != null && relation.getValidSince() != null) {
            validSinceByBer.putIfAbsent(relation.getBer(), relation.getValidSince().toLocalDateTime());
          }
        });

    return validSinceByBer;
  }

  public static Optional<LocalDateTime> getFarmToPersonRelationValidSince(AgisFarmType farm, String uid, @Nullable String ktIdP) {
    Predicate<AgisKtIdPRelationType> filterPredicate = ktIdP != null
        ? relation -> uid.equals(relation.getUid()) || ktIdP.equals(relation.getKtIdP())
        : relation -> uid.equals(relation.getUid());
    return AgisPersonFarmTreeUtils.getFarmToPersonRelations(farm).stream()
        .filter(filterPredicate)
        .map(AgisKtIdPRelationType::getValidSince)
        .filter(Objects::nonNull)
        .map(OffsetDateTime::toLocalDateTime)
        .findFirst();
  }

  public static List<AgisFarmType> getRelevantFarms(AgisPersonFarmTreeType personFarmTree) {
    return Optional.ofNullable(personFarmTree.getRelevantFarms())
        .map(AgisRelevantFarms::getFarm)
        .orElseGet(List::of);
  }

  private static List<AgisKtIdPRelationType> getFarmToPersonRelations(AgisFarmType farm) {
    return Optional.ofNullable(farm.getFarmToPersonRelations())
        .map(AgisFarmToPersonRelations::getFarmToPersonRelation)
        .orElseGet(List::of);
  }

  private static List<AgisKtIdBRelationType> getFarmParentRelations(AgisFarmType farm) {
    return Optional.ofNullable(farm.getFarmParentRelations())
        .map(AgisFarmParentRelations::getFarmParentRelation)
        .orElseGet(List::of);
  }

  private static List<AgisKtIdBRelationType> getFarmChildRelations(AgisFarmType farm) {
    return Optional.ofNullable(farm.getFarmChildRelations())
        .map(AgisFarmChildRelations::getFarmChildRelation)
        .orElseGet(List::of);
  }
}
