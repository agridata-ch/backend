package ch.agridata.agis.service;

import ch.agridata.agis.api.AgisApi;
import ch.agridata.agis.dto.AgisDataRequestType;
import ch.agridata.agis.dto.AgisFarmSearchParametersType;
import ch.agridata.agis.dto.AgisFarmType;
import ch.agridata.agis.dto.AgisPersonFarmResponseType;
import ch.agridata.agis.dto.AgisPersonFarmTreeType;
import ch.agridata.agis.dto.AgisPersonSearchParametersType;
import ch.agridata.agis.dto.AgisRegisterDataRequest;
import ch.agridata.agis.dto.AgisRelationDepthEnum;
import ch.agridata.agis.dto.AgisRelevantFarms;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Implements the {@link AgisApi} interface. It constructs appropriate request objects, calls the AGIS Register API via a REST client, and
 * processes results. It includes safeguards for ambiguous or missing farm data.
 *
 * @CommentLastReviewed 2025-08-25
 */

@ApplicationScoped
@Slf4j
public class AgisApiImpl implements AgisApi {

  private final AgisRegisterApiRestClient agisRegisterApiRestClient;

  @Inject
  public AgisApiImpl(@RestClient AgisRegisterApiRestClient agisRegisterApiRestClient) {
    this.agisRegisterApiRestClient = agisRegisterApiRestClient;
  }

  @Override
  public AgisPersonFarmResponseType fetchRegisterDataForKtIdP(@NonNull String ktIdP) {
    var findRegisterDataRequest = new AgisRegisterDataRequest();

    var dataRequestType = new AgisDataRequestType();
    dataRequestType.setRelationDepth(AgisRelationDepthEnum.ALL_RELATIONS);

    var personSearchParametersType = new AgisPersonSearchParametersType();
    personSearchParametersType.ktIdP(ktIdP);

    findRegisterDataRequest.dataRequestType(dataRequestType);
    findRegisterDataRequest.personSearchParameters(personSearchParametersType);

    return agisRegisterApiRestClient.register(findRegisterDataRequest);
  }

  @Override
  public AgisPersonFarmResponseType fetchRegisterDataForUid(@NonNull String uid) {
    var findRegisterDataRequest = new AgisRegisterDataRequest();

    var dataRequestType = new AgisDataRequestType();
    dataRequestType.setRelationDepth(AgisRelationDepthEnum.ALL_RELATIONS);

    var farmSearchParametersType = new AgisFarmSearchParametersType();
    farmSearchParametersType.uid(uid);

    findRegisterDataRequest.dataRequestType(dataRequestType);
    findRegisterDataRequest.farmSearchParameters(farmSearchParametersType);

    return agisRegisterApiRestClient.register(findRegisterDataRequest);
  }

  @Override
  public Optional<AgisFarmType> fetchFarmForBur(@NonNull String bur) {
    var findRegisterDataRequest = new AgisRegisterDataRequest();

    var dataRequestType = new AgisDataRequestType();

    var farmSearchParametersType = new AgisFarmSearchParametersType();
    farmSearchParametersType.ber(bur);

    findRegisterDataRequest.dataRequestType(dataRequestType);
    findRegisterDataRequest.farmSearchParameters(farmSearchParametersType);

    var farms = Optional.of(agisRegisterApiRestClient.register(findRegisterDataRequest).getPersonFarmTree())
        .map(AgisPersonFarmTreeType::getRelevantFarms)
        .map(AgisRelevantFarms::getFarm)
        .stream()
        .flatMap(List::stream)
        .toList();

    if (farms.isEmpty()) {
      log.warn("No farm found for bur {}", bur);
      return Optional.empty();
    }

    if (farms.size() > 1) {
      log.warn("Multiple farms found for bur {} ({} hits) — returning empty for safety", bur, farms.size());
      return Optional.empty();
    }

    return Optional.ofNullable(farms.getFirst());
  }
}
