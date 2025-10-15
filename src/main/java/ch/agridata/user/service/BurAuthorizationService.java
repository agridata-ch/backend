package ch.agridata.user.service;

import ch.agridata.agis.api.AgisApi;
import ch.agridata.agis.dto.AgisFarmType;
import ch.agridata.agis.dto.AgisPersonFarmResponseType;
import ch.agridata.agis.dto.AgisPersonFarmTreeType;
import ch.agridata.agis.dto.AgisRelevantFarms;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.user.dto.BurDto;
import ch.agridata.user.dto.FarmTypeEnum;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolves BURs authorized for a given UID by querying external register data. It maps register farms into BUR DTOs.
 *
 * @CommentLastReviewed 2025-08-25
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

    return Optional.of(personFarmTree)
        .map(AgisPersonFarmTreeType::getRelevantFarms)
        .map(AgisRelevantFarms::getFarm)
        .stream()
        .flatMap(List::stream)
        .map(this::mapToBurDto)
        .toList();
  }

  private BurDto mapToBurDto(@NonNull AgisFarmType farm) {
    return BurDto.builder()
        .bur(farm.getBer())
        .farmTypeCode(FarmTypeEnum.fromNumber(farm.getFarmType()))
        .build();
  }

}
