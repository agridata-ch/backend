package ch.agridata.user.utils;

import static org.assertj.core.api.Assertions.assertThat;

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
import ch.agridata.agis.dto.AgisRelevantPersons;
import ch.agridata.user.dto.BurParentLinkDto;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AgisPersonFarmTreeUtils}. They verify the indexing and lookup helpers used by {@link
 * ch.agridata.user.service.BurAuthorizationService} to navigate the AGIS person-farm tree. The AGIS types carry {@link OffsetDateTime}, while
 * the utils convert to {@link LocalDateTime} at the boundary, so fixtures use {@code OffsetDateTime} and assertions use {@code
 * LocalDateTime}.
 *
 * @CommentLastReviewed 2026-08-11
 */
class AgisPersonFarmTreeUtilsTest {

  private static final String UID = "CHE101000001";
  private static final String OTHER_UID = "CHE999999999";
  private static final OffsetDateTime VALID_SINCE = offsetDateTime(8, 23, 31);
  private static final OffsetDateTime OTHER_VALID_SINCE = offsetDateTime(9, 0, 0);
  private static final LocalDateTime LOCAL_VALID_SINCE = VALID_SINCE.toLocalDateTime();

  // indexFarmsByBer

  @Test
  void givenFarmsWithDistinctBers_whenIndexFarmsByBer_thenAllIndexed() {
    var farmA = farm("99910002");
    var farmB = farm("99910003");

    var result = AgisPersonFarmTreeUtils.indexFarmsByBer(List.of(farmA, farmB));

    assertThat(result).containsOnly(Map.entry("99910002", farmA), Map.entry("99910003", farmB));
  }

  @Test
  void givenFarmWithNullBer_whenIndexFarmsByBer_thenFarmIsSkipped() {
    var result = AgisPersonFarmTreeUtils.indexFarmsByBer(List.of(farm(null)));

    assertThat(result).isEmpty();
  }

  @Test
  void givenTwoFarmsWithSameBer_whenIndexFarmsByBer_thenFirstOneWins() {
    var first = farm("99910002");
    var second = farm("99910002");

    var result = AgisPersonFarmTreeUtils.indexFarmsByBer(List.of(first, second));

    assertThat(result.get("99910002")).isSameAs(first);
  }

  // indexParentsByChildBer

  @Test
  void givenFarmWithFarmParentRelation_whenIndexParentsByChildBer_thenChildMapsToParent() {
    var child = farm("99910003").farmParentRelations(parentRelations(farmRelation("99910002", VALID_SINCE)));

    var result = AgisPersonFarmTreeUtils.indexParentsByChildBer(List.of(child));

    assertThat(result).containsEntry("99910003", new BurParentLinkDto("99910002", LOCAL_VALID_SINCE));
  }

  @Test
  void givenFarmWithFarmChildRelation_whenIndexParentsByChildBer_thenChildMapsToParent() {
    var parent = farm("99910002").farmChildRelations(childRelations(farmRelation("99910003", VALID_SINCE)));

    var result = AgisPersonFarmTreeUtils.indexParentsByChildBer(List.of(parent));

    assertThat(result).containsEntry("99910003", new BurParentLinkDto("99910002", LOCAL_VALID_SINCE));
  }

  @Test
  void givenBothDirectionsForSameEdge_whenIndexParentsByChildBer_thenResultHasSingleConsistentEntry() {
    var parent = farm("99910002").farmChildRelations(childRelations(farmRelation("99910003", VALID_SINCE)));
    var child = farm("99910003").farmParentRelations(parentRelations(farmRelation("99910002", VALID_SINCE)));

    var result = AgisPersonFarmTreeUtils.indexParentsByChildBer(List.of(parent, child));

    assertThat(result).containsEntry("99910003", new BurParentLinkDto("99910002", LOCAL_VALID_SINCE));
  }

  @Test
  void givenRelationWithNullBer_whenIndexParentsByChildBer_thenRelationIsSkipped() {
    var child = farm("99910003").farmParentRelations(parentRelations(farmRelation(null, VALID_SINCE)));

    var result = AgisPersonFarmTreeUtils.indexParentsByChildBer(List.of(child));

    assertThat(result).isEmpty();
  }

  @Test
  void givenRelationWithNullValidSince_whenIndexParentsByChildBer_thenRelationIsSkipped() {
    var child = farm("99910003").farmParentRelations(parentRelations(farmRelation("99910002", null)));

    var result = AgisPersonFarmTreeUtils.indexParentsByChildBer(List.of(child));

    assertThat(result).isEmpty();
  }

  @Test
  void givenFarmWithoutParentOrChildRelations_whenIndexParentsByChildBer_thenResultIsEmpty() {
    var result = AgisPersonFarmTreeUtils.indexParentsByChildBer(List.of(farm("99910002")));

    assertThat(result).isEmpty();
  }

  // indexPersonToFarmValidSince

  @Test
  void givenPersonToFarmRelationForTargetUid_whenIndexPersonToFarmValidSince_thenIndexedByBer() {
    var tree = personFarmTree(List.of(), List.of(person(UID, farmRelation("99910002", VALID_SINCE))));

    var result = AgisPersonFarmTreeUtils.indexPersonToFarmValidSince(tree, UID);

    assertThat(result).containsEntry("99910002", LOCAL_VALID_SINCE);
  }

  @Test
  void givenPersonToFarmRelationForOtherUid_whenIndexPersonToFarmValidSince_thenNotIndexed() {
    var tree = personFarmTree(List.of(), List.of(person(OTHER_UID, farmRelation("99910002", VALID_SINCE))));

    var result = AgisPersonFarmTreeUtils.indexPersonToFarmValidSince(tree, UID);

    assertThat(result).isEmpty();
  }

  @Test
  void givenNoRelevantPersons_whenIndexPersonToFarmValidSince_thenResultIsEmpty() {
    var tree = new AgisPersonFarmTreeType();

    var result = AgisPersonFarmTreeUtils.indexPersonToFarmValidSince(tree, UID);

    assertThat(result).isEmpty();
  }

  @Test
  void givenPersonWithoutPersonToFarmRelations_whenIndexPersonToFarmValidSince_thenResultIsEmpty() {
    var tree = personFarmTree(List.of(), List.of(new AgisPersonType().uid(UID)));

    var result = AgisPersonFarmTreeUtils.indexPersonToFarmValidSince(tree, UID);

    assertThat(result).isEmpty();
  }

  @Test
  void givenRelationWithNullBerOrValidSince_whenIndexPersonToFarmValidSince_thenRelationIsSkipped() {
    var tree = personFarmTree(List.of(), List.of(person(UID, farmRelation(null, VALID_SINCE), farmRelation("99910002", null))));

    var result = AgisPersonFarmTreeUtils.indexPersonToFarmValidSince(tree, UID);

    assertThat(result).isEmpty();
  }

  @Test
  void givenTwoRelationsForSameBer_whenIndexPersonToFarmValidSince_thenFirstOneWins() {
    var tree = personFarmTree(
        List.of(),
        List.of(person(UID, farmRelation("99910002", VALID_SINCE), farmRelation("99910002", OTHER_VALID_SINCE)))
    );

    var result = AgisPersonFarmTreeUtils.indexPersonToFarmValidSince(tree, UID);

    assertThat(result).containsEntry("99910002", LOCAL_VALID_SINCE);
  }

  // getRelevantFarms

  @Test
  void givenRelevantFarms_whenGetRelevantFarms_thenAllReturned() {
    var farmA = farm("99910002");
    var farmB = farm("99910003");
    var tree = personFarmTree(List.of(farmA, farmB), List.of());

    assertThat(AgisPersonFarmTreeUtils.getRelevantFarms(tree)).containsExactly(farmA, farmB);
  }

  @Test
  void givenNoRelevantFarms_whenGetRelevantFarms_thenEmptyList() {
    assertThat(AgisPersonFarmTreeUtils.getRelevantFarms(new AgisPersonFarmTreeType())).isEmpty();
  }

  // getFarmToPersonRelationValidSince

  @Test
  void givenFarmToPersonRelationForUid_whenGetFarmToPersonRelationValidSince_thenValidSinceReturned() {
    var farm = farm("99910002").farmToPersonRelations(farmToPersonRelations(relation(UID, VALID_SINCE)));

    assertThat(AgisPersonFarmTreeUtils.getFarmToPersonRelationValidSince(farm, UID)).contains(LOCAL_VALID_SINCE);
  }

  @Test
  void givenFarmToPersonRelationForOtherUid_whenGetFarmToPersonRelationValidSince_thenEmpty() {
    var farm = farm("99910002").farmToPersonRelations(farmToPersonRelations(relation(OTHER_UID, VALID_SINCE)));

    assertThat(AgisPersonFarmTreeUtils.getFarmToPersonRelationValidSince(farm, UID)).isEmpty();
  }

  @Test
  void givenFarmWithoutFarmToPersonRelations_whenGetFarmToPersonRelationValidSince_thenEmpty() {
    assertThat(AgisPersonFarmTreeUtils.getFarmToPersonRelationValidSince(farm("99910002"), UID)).isEmpty();
  }

  @Test
  void givenRelationWithNullValidSince_whenGetFarmToPersonRelationValidSince_thenSkippedInFavorOfNextMatch() {
    var farm = farm("99910002")
        .farmToPersonRelations(farmToPersonRelations(relation(UID, null), relation(UID, VALID_SINCE)));

    assertThat(AgisPersonFarmTreeUtils.getFarmToPersonRelationValidSince(farm, UID)).contains(LOCAL_VALID_SINCE);
  }

  private static AgisPersonFarmTreeType personFarmTree(List<AgisFarmType> farms, List<AgisPersonType> persons) {
    return new AgisPersonFarmTreeType()
        .relevantFarms(new AgisRelevantFarms().farm(farms))
        .relevantPersons(new AgisRelevantPersons().person(persons));
  }

  private static AgisFarmType farm(String ber) {
    return new AgisFarmType().ber(ber).uid(UID).farmType("01");
  }

  private static AgisPersonType person(String uid, AgisKtIdBRelationType... relations) {
    return new AgisPersonType().uid(uid)
        .personToFarmRelations(new AgisPersonToFarmRelations().personToFarmRelation(Arrays.asList(relations)));
  }

  private static AgisFarmToPersonRelations farmToPersonRelations(AgisKtIdPRelationType... relations) {
    return new AgisFarmToPersonRelations().farmToPersonRelation(Arrays.asList(relations));
  }

  private static AgisFarmParentRelations parentRelations(AgisKtIdBRelationType... relations) {
    return new AgisFarmParentRelations().farmParentRelation(Arrays.asList(relations));
  }

  private static AgisFarmChildRelations childRelations(AgisKtIdBRelationType... relations) {
    return new AgisFarmChildRelations().farmChildRelation(Arrays.asList(relations));
  }

  private static AgisKtIdPRelationType relation(String uid, OffsetDateTime validSince) {
    return new AgisKtIdPRelationType().uid(uid).validSince(validSince);
  }

  private static AgisKtIdBRelationType farmRelation(String ber, OffsetDateTime validSince) {
    return new AgisKtIdBRelationType().ber(ber).validSince(validSince);
  }

  private static OffsetDateTime offsetDateTime(int hour, int minute, int second) {
    return OffsetDateTime.of(2025, 12, 11, hour, minute, second, 0, ZoneOffset.ofHours(1));
  }
}
