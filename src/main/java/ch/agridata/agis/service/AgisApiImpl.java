package ch.agridata.agis.service;

import ch.agridata.agis.api.AgisApi;
import ch.agridata.agis.dto.AgisDataRequestType;
import ch.agridata.agis.dto.AgisFarmOwnershipDto;
import ch.agridata.agis.dto.AgisFarmSearchParametersType;
import ch.agridata.agis.dto.AgisFarmType;
import ch.agridata.agis.dto.AgisPersonFarmResponseType;
import ch.agridata.agis.dto.AgisPersonFarmTreeType;
import ch.agridata.agis.dto.AgisPersonSearchParametersType;
import ch.agridata.agis.dto.AgisRegisterDataRequest;
import ch.agridata.agis.dto.AgisRegisterMutationDataRequest;
import ch.agridata.agis.dto.AgisRelationDepthEnum;
import ch.agridata.agis.dto.AgisRelevantFarms;
import ch.agridata.agis.dto.AgisRequestOffsetType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
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

  private static final int CHUNK_SIZE = 500;

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

  private AgisPersonFarmResponseType fetchChunk(
      LocalDate from,
      LocalDate to,
      int offsetFrom,
      int offsetTo,
      AgisRegisterMutationDataRequest.MutationTypeEnum mutationType,
      AgisRegisterMutationDataRequest.MutationDataTypeEnum mutationDataType
  ) {
    var findRegisterMutationDataRequest = new AgisRegisterMutationDataRequest();

    findRegisterMutationDataRequest.setFrom(from);
    findRegisterMutationDataRequest.setTo(to);
    findRegisterMutationDataRequest.mutationType(mutationType);
    findRegisterMutationDataRequest.mutationDataType(mutationDataType);

    AgisRequestOffsetType resultOffset = new AgisRequestOffsetType();
    resultOffset.setFrom(offsetFrom);
    resultOffset.setTo(offsetTo);
    findRegisterMutationDataRequest.setResultOffset(resultOffset);

    return agisRegisterApiRestClient.registerMutation(findRegisterMutationDataRequest);
  }

  private List<AgisFarmOwnershipDto> fetchAllChunks(
      LocalDate from,
      LocalDate to,
      AgisRegisterMutationDataRequest.MutationTypeEnum mutationType,
      AgisRegisterMutationDataRequest.MutationDataTypeEnum mutationDataType
  ) {
    List<AgisFarmOwnershipDto> result = new ArrayList<>();

    int currentFrom = 0;
    int totalHits = Integer.MAX_VALUE;

    while (currentFrom < totalHits) {

      int currentTo = currentFrom + CHUNK_SIZE;

      AgisPersonFarmResponseType response = fetchChunk(
          from,
          to,
          currentFrom,
          currentTo,
          mutationType,
          mutationDataType
      );

      result.addAll(extractAgisFarmTypes(response));

      totalHits =
          response != null
              && response.getResultOffset() != null
              && response.getResultOffset().getTotalHits() != null
              ? response.getResultOffset().getTotalHits()
              : 0;

      currentFrom += CHUNK_SIZE;
    }

    return result;
  }

  public List<AgisFarmOwnershipDto> fetchFarmMutations(LocalDate from, LocalDate to) {
    return fetchAllChunks(
        from,
        to,
        AgisRegisterMutationDataRequest.MutationTypeEnum.MODIFIED,
        AgisRegisterMutationDataRequest.MutationDataTypeEnum.FARM);
  }

  public List<String> fetchFarmDeletions(LocalDate from, LocalDate to) {
    var farmOwnerships = fetchAllChunks(
        from,
        to,
        AgisRegisterMutationDataRequest.MutationTypeEnum.DELETED,
        AgisRegisterMutationDataRequest.MutationDataTypeEnum.FARM
    );
    return farmOwnerships.stream().map(AgisFarmOwnershipDto::bur).toList();
  }

  private List<AgisFarmOwnershipDto> extractAgisFarmTypes(AgisPersonFarmResponseType response) {
    if (response == null
        || response.getPersonFarmTree() == null
        || response.getPersonFarmTree().getRelevantFarms() == null
        || response.getPersonFarmTree().getRelevantFarms().getFarm() == null) {
      return List.of();
    }

    return response
        .getPersonFarmTree()
        .getRelevantFarms()
        .getFarm()
        .stream()
        .map(farm -> new AgisFarmOwnershipDto(
            farm.getBer(),
            farm.getUid()
        ))
        .toList();
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
