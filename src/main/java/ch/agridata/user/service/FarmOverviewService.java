package ch.agridata.user.service;

import ch.agridata.agis.api.AgisApi;
import ch.agridata.agis.dto.AgisFarmType;
import ch.agridata.user.dto.BurDto;
import ch.agridata.user.dto.FarmOverviewDto;
import ch.agridata.user.mapper.FarmOverviewMapper;
import ch.agridata.user.utils.AgisPersonFarmTreeUtils;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Builds the farm overview ("Mein Betrieb") of a company: the company with its name and address, and every BUR the company is authorized
 * for with its KT_ID_B, farm type and address. Equid owners (UIDs starting with "ZZZ") are not known to AGIS, so they get the company only.
 *
 * @CommentLastReviewed 2026-11-23
 */
@ApplicationScoped
@RequiredArgsConstructor
public class FarmOverviewService {

  private final AgisApi agisApi;
  private final UidAuthorizationService uidAuthorizationService;
  private final BurAuthorizationService burAuthorizationService;
  private final FarmOverviewMapper farmOverviewMapper;

  public FarmOverviewDto getFarmOverviewForProducer(@NonNull String uid, @Nullable String ktIdP, @Nullable String agateLoginId) {
    var authorizedUid = uidAuthorizationService.findUidIfAuthorized(uid, ktIdP, agateLoginId).orElseThrow(ForbiddenException::new);
    return getFarmOverview(uid, authorizedUid.name());
  }

  /**
   * Returns the farm overview of any UID, without authorization check (admin use).
   */
  public FarmOverviewDto getFarmOverview(@NonNull String uid) {
    return getFarmOverview(uid, null);
  }

  private FarmOverviewDto getFarmOverview(String uid, @Nullable String knownName) {
    // AGIS only accepts UIDs starting with "CHE"; equid owners have a "ZZZ" UID from Identitas and no BURs.
    if (uid.startsWith("ZZZ")) {
      return new FarmOverviewDto(farmOverviewMapper.toCompanyDto(uid, knownName, null), List.of());
    }

    var personFarmTree = AgisPersonFarmTreeUtils.getPersonFarmTree(agisApi.fetchRegisterDataForUid(uid));

    var person = AgisPersonFarmTreeUtils.findRelevantPersonByUid(personFarmTree, uid);
    if (person.isEmpty() && AgisPersonFarmTreeUtils.getRelevantFarms(personFarmTree).isEmpty()) {
      throw new NotFoundException("No register data found for uid " + uid);
    }
    var postalAddress = person.flatMap(AgisPersonFarmTreeUtils::getPostalAddress);
    var name = Optional.ofNullable(knownName).or(() -> postalAddress.flatMap(farmOverviewMapper::toName)).orElse(null);
    var company = farmOverviewMapper.toCompanyDto(uid, name, postalAddress.orElse(null));

    var authorizedBurs = burAuthorizationService.resolveAuthorizedBursFromPersonFarmTree(uid, personFarmTree).stream()
        .map(BurDto::bur)
        .collect(Collectors.toSet());
    var farms = AgisPersonFarmTreeUtils.indexFarmsByBer(AgisPersonFarmTreeUtils.getRelevantFarms(personFarmTree)).values().stream()
        .filter(farm -> authorizedBurs.contains(farm.getBer()))
        .sorted(Comparator.comparing(AgisFarmType::getBer))
        .map(farmOverviewMapper::toFarmDto)
        .toList();

    return new FarmOverviewDto(company, farms);
  }
}
