package ch.agridata.user.service;

import static ch.agridata.user.dto.FarmTypeEnum.GANZJAHRESBETRIEB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

import ch.agridata.agis.api.AgisApi;
import ch.agridata.agis.dto.AgisFarmChildRelations;
import ch.agridata.agis.dto.AgisFarmParentRelations;
import ch.agridata.agis.dto.AgisFarmToPersonRelations;
import ch.agridata.agis.dto.AgisFarmType;
import ch.agridata.agis.dto.AgisKtIdBRelationType;
import ch.agridata.agis.dto.AgisKtIdPRelationType;
import ch.agridata.agis.dto.AgisPersonFarmResponseType;
import ch.agridata.agis.dto.AgisPersonFarmTreeType;
import ch.agridata.agis.dto.AgisPersonToFarmRelations;
import ch.agridata.agis.dto.AgisPersonType;
import ch.agridata.agis.dto.AgisRelevantFarms;
import ch.agridata.agis.dto.AgisRelevantPersons;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.user.dto.BurDto;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link BurAuthorizationService}. They verify how register farms are mapped to BUR DTOs, focusing on the three rules used to
 * resolve the relation start date: the farm-to-person relation, the person-to-farm relation and the fallback via the parent farm.
 *
 * @CommentLastReviewed 2026-08-10
 */
@ExtendWith(MockitoExtension.class)
class BurAuthorizationServiceTest {

  private static final String UID = "CHE101000001";
  private static final String OTHER_UID = "CHE999999999";
  private static final String PARENT_BER = "99910002";
  private static final String CHILD_BER = "99910003";

  private static final OffsetDateTime VALID_SINCE = offsetDateTime(8, 23, 31);
  private static final OffsetDateTime LATER = offsetDateTime(9, 0, 0);
  private static final OffsetDateTime EVEN_LATER = offsetDateTime(10, 0, 0);

  @Mock
  AgisApi agisApi;

  @InjectMocks
  BurAuthorizationService burAuthorizationService;

  @Test
  void givenFarmWithFarmToPersonRelation_whenGetAuthorizedBurs_thenRelationSinceFromThatRelation() {
    var farm = farm(PARENT_BER).farmToPersonRelations(farmToPersonRelations(relation(UID, VALID_SINCE)));
    when(agisApi.fetchRegisterDataForUid(UID)).thenReturn(response(List.of(farm), null));

    var result = burAuthorizationService.getAuthorizedBurs(UID);

    assertThat(result).containsExactly(BurDto.builder()
        .bur(PARENT_BER)
        .farmTypeCode(GANZJAHRESBETRIEB)
        .uid(UID)
        .relationSince(localDateTime(8, 23, 31))
        .build());
  }

  @Test
  void givenFarmToPersonRelationForOtherUid_whenGetAuthorizedBurs_thenFarmIsSkipped() {
    var farm = farm(PARENT_BER).farmToPersonRelations(farmToPersonRelations(relation(OTHER_UID, VALID_SINCE)));
    when(agisApi.fetchRegisterDataForUid(UID)).thenReturn(response(List.of(farm), null));

    assertThat(burAuthorizationService.getAuthorizedBurs(UID)).isEmpty();
  }

  @Test
  void givenNoUsableRelationAtAll_whenGetAuthorizedBurs_thenFarmIsSkipped() {
    when(agisApi.fetchRegisterDataForUid(UID)).thenReturn(response(List.of(farm(PARENT_BER)), null));

    assertThat(burAuthorizationService.getAuthorizedBurs(UID)).isEmpty();
  }

  @Test
  void givenOnlyPersonToFarmRelation_whenGetAuthorizedBurs_thenRelationSinceFromPersonRelation() {
    when(agisApi.fetchRegisterDataForUid(UID))
        .thenReturn(response(List.of(farm(PARENT_BER)), person(UID, farmRelation(PARENT_BER, VALID_SINCE))));

    assertThat(burAuthorizationService.getAuthorizedBurs(UID))
        .singleElement()
        .satisfies(bur -> assertThat(bur.relationSince()).isEqualTo(localDateTime(8, 23, 31)));
  }

  @Test
  void givenPersonToFarmRelationOfOtherPerson_whenGetAuthorizedBurs_thenFarmIsSkipped() {
    when(agisApi.fetchRegisterDataForUid(UID))
        .thenReturn(response(List.of(farm(PARENT_BER)), person(OTHER_UID, farmRelation(PARENT_BER, VALID_SINCE))));

    assertThat(burAuthorizationService.getAuthorizedBurs(UID)).isEmpty();
  }

  @Test
  void givenChildWithParentRelation_whenGetAuthorizedBurs_thenRelationSinceFromParent() {
    var parent = farm(PARENT_BER).farmToPersonRelations(farmToPersonRelations(relation(UID, VALID_SINCE)));
    var child = farm(CHILD_BER).farmParentRelations(parentRelations(farmRelation(PARENT_BER, LATER)));
    when(agisApi.fetchRegisterDataForUid(UID)).thenReturn(response(List.of(parent, child), null));

    assertThat(burAuthorizationService.getAuthorizedBurs(UID))
        .extracting(BurDto::bur, BurDto::relationSince)
        .containsExactly(
            tuple(PARENT_BER, localDateTime(8, 23, 31)),
            tuple(CHILD_BER, localDateTime(9, 0, 0)));
  }

  @Test
  void givenChildOnlyListedByParentChildRelation_whenGetAuthorizedBurs_thenRelationSinceFromParent() {
    var parent = farm(PARENT_BER)
        .farmToPersonRelations(farmToPersonRelations(relation(UID, VALID_SINCE)))
        .farmChildRelations(childRelations(farmRelation(CHILD_BER, LATER)));
    var child = farm(CHILD_BER);
    when(agisApi.fetchRegisterDataForUid(UID)).thenReturn(response(List.of(parent, child), null));

    assertThat(burAuthorizationService.getAuthorizedBurs(UID))
        .filteredOn(bur -> CHILD_BER.equals(bur.bur()))
        .singleElement()
        .satisfies(bur -> assertThat(bur.relationSince()).isEqualTo(localDateTime(9, 0, 0)));
  }

  @Test
  void givenParentDateLaterThanRelationDate_whenGetAuthorizedBurs_thenParentDateWins() {
    var parent = farm(PARENT_BER).farmToPersonRelations(farmToPersonRelations(relation(UID, EVEN_LATER)));
    var child = farm(CHILD_BER).farmParentRelations(parentRelations(farmRelation(PARENT_BER, LATER)));
    when(agisApi.fetchRegisterDataForUid(UID)).thenReturn(response(List.of(parent, child), null));

    assertThat(relationSinceOf(burAuthorizationService.getAuthorizedBurs(UID), CHILD_BER)).isEqualTo(localDateTime(10, 0, 0));
  }

  @Test
  void givenRelationDateLaterThanParentDate_whenGetAuthorizedBurs_thenRelationDateWins() {
    var parent = farm(PARENT_BER).farmToPersonRelations(farmToPersonRelations(relation(UID, VALID_SINCE)));
    var child = farm(CHILD_BER).farmParentRelations(parentRelations(farmRelation(PARENT_BER, EVEN_LATER)));
    when(agisApi.fetchRegisterDataForUid(UID)).thenReturn(response(List.of(parent, child), null));

    assertThat(relationSinceOf(burAuthorizationService.getAuthorizedBurs(UID), CHILD_BER)).isEqualTo(localDateTime(10, 0, 0));
  }

  @Test
  void givenMultiLevelChain_whenGetAuthorizedBurs_thenLatestDateAlongChainWins() {
    var grandParent = farm("99910001").farmToPersonRelations(farmToPersonRelations(relation(UID, VALID_SINCE)));
    var parent = farm(PARENT_BER).farmParentRelations(parentRelations(farmRelation("99910001", LATER)));
    var child = farm(CHILD_BER).farmParentRelations(parentRelations(farmRelation(PARENT_BER, VALID_SINCE)));
    when(agisApi.fetchRegisterDataForUid(UID)).thenReturn(response(List.of(grandParent, parent, child), null));

    assertThat(relationSinceOf(burAuthorizationService.getAuthorizedBurs(UID), CHILD_BER)).isEqualTo(localDateTime(9, 0, 0));
  }

  @Test
  void givenParentNotInRelevantFarms_whenGetAuthorizedBurs_thenFarmIsSkipped() {
    var child = farm(CHILD_BER).farmParentRelations(parentRelations(farmRelation(PARENT_BER, LATER)));
    when(agisApi.fetchRegisterDataForUid(UID)).thenReturn(response(List.of(child), null));

    assertThat(burAuthorizationService.getAuthorizedBurs(UID)).isEmpty();
  }

  @Test
  void givenCyclicParentRelations_whenGetAuthorizedBurs_thenFarmsAreSkippedWithoutInfiniteRecursion() {
    var first = farm(PARENT_BER).farmParentRelations(parentRelations(farmRelation(CHILD_BER, LATER)));
    var second = farm(CHILD_BER).farmParentRelations(parentRelations(farmRelation(PARENT_BER, LATER)));
    when(agisApi.fetchRegisterDataForUid(UID)).thenReturn(response(List.of(first, second), null));

    assertThat(burAuthorizationService.getAuthorizedBurs(UID)).isEmpty();
  }

  @Test
  void givenFarmToPersonAndParentRelation_whenGetAuthorizedBurs_thenFarmToPersonRelationWins() {
    var parent = farm(PARENT_BER).farmToPersonRelations(farmToPersonRelations(relation(UID, EVEN_LATER)));
    var child = farm(CHILD_BER)
        .farmToPersonRelations(farmToPersonRelations(relation(UID, VALID_SINCE)))
        .farmParentRelations(parentRelations(farmRelation(PARENT_BER, LATER)));
    when(agisApi.fetchRegisterDataForUid(UID)).thenReturn(response(List.of(parent, child), null));

    assertThat(relationSinceOf(burAuthorizationService.getAuthorizedBurs(UID), CHILD_BER)).isEqualTo(localDateTime(8, 23, 31));
  }

  @Test
  void givenParentListedLastInRelevantFarms_whenGetAuthorizedBurs_thenResultIsIndependentOfOrder() {
    var parent = farm(PARENT_BER).farmToPersonRelations(farmToPersonRelations(relation(UID, VALID_SINCE)));
    var child = farm(CHILD_BER).farmParentRelations(parentRelations(farmRelation(PARENT_BER, LATER)));
    when(agisApi.fetchRegisterDataForUid(UID)).thenReturn(response(List.of(child, parent), null));

    assertThat(relationSinceOf(burAuthorizationService.getAuthorizedBurs(UID), CHILD_BER)).isEqualTo(localDateTime(9, 0, 0));
  }

  @Test
  void givenNoPersonFarmTree_whenGetAuthorizedBurs_thenThrowExternalWebServiceException() {
    when(agisApi.fetchRegisterDataForUid(UID)).thenReturn(new AgisPersonFarmResponseType());

    assertThatThrownBy(() -> burAuthorizationService.getAuthorizedBurs(UID))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("no personFarmTree found");
  }

  @Test
  void givenNullUid_whenGetAuthorizedBurs_thenThrowNullPointerException() {
    assertThatThrownBy(() -> burAuthorizationService.getAuthorizedBurs(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void givenNullFarmInResponse_whenGetAuthorizedBurs_thenThrowNullPointerException() {
    when(agisApi.fetchRegisterDataForUid(UID)).thenReturn(response(Collections.singletonList(null), null));

    assertThatThrownBy(() -> burAuthorizationService.getAuthorizedBurs(UID))
        .isInstanceOf(NullPointerException.class);
  }

  private static LocalDateTime relationSinceOf(List<BurDto> burs, String ber) {
    return burs.stream()
        .filter(bur -> ber.equals(bur.bur()))
        .findFirst()
        .orElseThrow()
        .relationSince();
  }

  private static AgisPersonFarmResponseType response(List<AgisFarmType> farms, AgisPersonType person) {
    var personFarmTree = new AgisPersonFarmTreeType().relevantFarms(new AgisRelevantFarms().farm(farms));

    if (person != null) {
      personFarmTree.relevantPersons(new AgisRelevantPersons().person(List.of(person)));
    }

    return new AgisPersonFarmResponseType().personFarmTree(personFarmTree);
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

  private static LocalDateTime localDateTime(int hour, int minute, int second) {
    return LocalDateTime.of(2025, 12, 11, hour, minute, second);
  }
}
