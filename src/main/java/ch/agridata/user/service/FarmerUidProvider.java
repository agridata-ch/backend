package ch.agridata.user.service;

import ch.agridata.agis.api.AgisApi;
import ch.agridata.agis.dto.AgisAddressType;
import ch.agridata.agis.dto.AgisContactType;
import ch.agridata.agis.dto.AgisFarmRelations;
import ch.agridata.agis.dto.AgisFarmToPersonRelations;
import ch.agridata.agis.dto.AgisFarmType;
import ch.agridata.agis.dto.AgisKtIdPRelationType;
import ch.agridata.agis.dto.AgisMailAddressType;
import ch.agridata.agis.dto.AgisOrganisationMailAddressInfoType;
import ch.agridata.agis.dto.AgisPersonFarmResponseType;
import ch.agridata.agis.dto.AgisPersonFarmTreeType;
import ch.agridata.agis.dto.AgisPersonParentRelations;
import ch.agridata.agis.dto.AgisPersonRelations;
import ch.agridata.agis.dto.AgisPersonType;
import ch.agridata.agis.dto.AgisRelevantFarms;
import ch.agridata.agis.dto.AgisRelevantPersons;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.user.dto.LegalFormEnum;
import ch.agridata.user.dto.UidDto;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

/**
 * Resolves UIDs authorized for a given KtIdP. It traverses relationships in register data, identifies linked farms and persons, and
 * enriches UIDs with name and legal form.
 *
 * @CommentLastReviewed 2025-08-25
 */

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class FarmerUidProvider {

  private final AgisApi agisApi;

  @Retry
  @Timeout(value = 2, unit = ChronoUnit.SECONDS)
  public List<UidDto> getAuthorizedUids(@NonNull String ktIdP) {
    var personFarmResponse = agisApi.fetchRegisterDataForKtIdP(ktIdP);
    return findAuthorizedUids(personFarmResponse);
  }

  private List<UidDto> findAuthorizedUids(@NonNull AgisPersonFarmResponseType response) {
    var personFarmTree = Optional.of(response)
        .map(AgisPersonFarmResponseType::getPersonFarmTree)
        .orElseThrow(() -> new ExternalWebServiceException("invalid response from AGIS: no personFarmTree found", null));

    var matchedPersonKtIdP = Optional.of(personFarmTree)
        .map(AgisPersonFarmTreeType::getRelevantPersons)
        .map(AgisRelevantPersons::getPerson)
        .stream()
        .flatMap(List::stream)
        .findFirst()
        .map(AgisPersonType::getKtIdP)
        .orElseThrow(() -> new ExternalWebServiceException("invalid response from AGIS: no person found", null));

    var allPersons = Stream.concat(
            Stream.of(personFarmTree)
                .map(AgisPersonFarmTreeType::getRelevantPersons)
                .map(AgisRelevantPersons::getPerson),
            Stream.of(personFarmTree)
                .map(AgisPersonFarmTreeType::getPersonRelations)
                .map(AgisPersonRelations::getPerson))
        .flatMap(List::stream)
        .toList();

    return findKtIdPsOfPerson(matchedPersonKtIdP, allPersons, new HashSet<>())
        .flatMap(id -> findFarmsWithPersonRelationToKtIdP(personFarmTree, id))
        .peek(farm -> log.debug("Found farm with BUR: {}", farm.getBer())) // NOSONAR: peek() is only used for debugging purposes
        .map(AgisFarmType::getUid)
        .peek(uid -> log.debug("Found UID: {}", uid)) // NOSONAR: peek() is only used for debugging purposes
        .filter(Objects::nonNull)
        .distinct()
        .map(uid -> UidDto.builder()
            .uid(uid)
            .name(findNameOfUid(personFarmTree, uid).orElse(""))
            .legalFormCode(findLegalFormOfUid(personFarmTree, uid).orElse(LegalFormEnum.UNKNOWN))
            .build())
        .toList();
  }

  private Stream<String> findKtIdPsOfPerson(String ktIdP,
                                            List<AgisPersonType> persons,
                                            Set<String> visited) {
    if (ktIdP == null || !visited.add(ktIdP)) {
      return Stream.empty();
    }

    Stream<String> self = Stream.of(ktIdP);

    Stream<String> parents = persons.stream()
        .filter(p -> Objects.equals(p.getKtIdP(), ktIdP))
        .map(AgisPersonType::getPersonParentRelations)
        .filter(Objects::nonNull)
        .map(AgisPersonParentRelations::getPersonParentRelation)
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .map(AgisKtIdPRelationType::getKtIdP)
        .flatMap(parent -> findKtIdPsOfPerson(parent, persons, visited));

    return Stream.concat(self, parents);
  }


  private Stream<AgisFarmType> findFarmsWithPersonRelationToKtIdP(@NonNull AgisPersonFarmTreeType personFarmTree,
                                                                  @NonNull String ktIdP) {

    return Stream.concat(
            Optional.of(personFarmTree)
                .map(AgisPersonFarmTreeType::getRelevantFarms)
                .map(AgisRelevantFarms::getFarm).stream(),
            Optional.of(personFarmTree)
                .map(AgisPersonFarmTreeType::getFarmRelations)
                .map(AgisFarmRelations::getFarm).stream())
        .flatMap(List::stream)
        .filter(farm -> farmHasRelationToKtIdP(farm, ktIdP));
  }

  private boolean farmHasRelationToKtIdP(@NonNull AgisFarmType farm,
                                         @NonNull String ktIdP) {

    return Optional.of(farm)
        .map(AgisFarmType::getFarmToPersonRelations)
        .map(AgisFarmToPersonRelations::getFarmToPersonRelation)
        .stream()
        .flatMap(List::stream)
        .map(AgisKtIdPRelationType::getKtIdP)
        .anyMatch(ktIdP::equals);
  }

  private Optional<String> findNameOfUid(@NonNull AgisPersonFarmTreeType personFarmTree,
                                         @NonNull String uid) {

    var responsiblePerson = findResponsiblePersonOfUid(personFarmTree, uid);

    if (responsiblePerson.isEmpty()) {
      return Optional.empty();
    }

    var postalAddress = responsiblePerson
        .map(AgisPersonType::getContact)
        .map(AgisContactType::getAddress)
        .map(AgisAddressType::getPostalAddress);

    var name = postalAddress
        .map(AgisMailAddressType::getPerson)
        .map(p -> p.getFirstName() + " " + p.getLastName())
        .or(() -> postalAddress
            .map(AgisMailAddressType::getOrganisation)
            .map(AgisOrganisationMailAddressInfoType::getOrganisationName)
        );

    if (name.isPresent()) {
      log.debug("Resolved name for UID {}: {}", uid, name.get());
    } else {
      log.warn("Could not resolve name for UID {}", uid);
    }

    return name;
  }

  private Optional<LegalFormEnum> findLegalFormOfUid(@NonNull AgisPersonFarmTreeType personFarmTree,
                                                     @NonNull String uid) {

    return findResponsiblePersonOfUid(personFarmTree, uid)
        .map(AgisPersonType::getLegalForm)
        .map(LegalFormEnum::fromNumber);
  }

  private Optional<AgisPersonType> findResponsiblePersonOfUid(@NonNull AgisPersonFarmTreeType personFarmTree,
                                                              @NonNull String uid) {
    var responsibleKtIdP = findResponsibleKtIdPofUid(personFarmTree, uid);

    if (responsibleKtIdP.isEmpty()) {
      return Optional.empty();
    }

    var responsiblePerson = Stream.concat(
            Optional.of(personFarmTree)
                .map(AgisPersonFarmTreeType::getRelevantPersons)
                .map(AgisRelevantPersons::getPerson).stream(),
            Optional.of(personFarmTree)
                .map(AgisPersonFarmTreeType::getPersonRelations)
                .map(AgisPersonRelations::getPerson).stream())
        .flatMap(List::stream)
        .filter(person -> responsibleKtIdP.get().equals(person.getKtIdP()))
        .findFirst();

    if (responsiblePerson.isEmpty()) {
      log.warn("Could not resolve responsible person for ktIdP {}", responsibleKtIdP.get());
    }

    return responsiblePerson;
  }

  private Optional<String> findResponsibleKtIdPofUid(@NonNull AgisPersonFarmTreeType personFarmTree,
                                                     @NonNull String uid) {
    var responsibleKtIdP = Stream.concat(
            Optional.of(personFarmTree)
                .map(AgisPersonFarmTreeType::getRelevantFarms)
                .map(AgisRelevantFarms::getFarm).stream(),
            Optional.of(personFarmTree)
                .map(AgisPersonFarmTreeType::getFarmRelations)
                .map(AgisFarmRelations::getFarm).stream())
        .flatMap(List::stream)
        .filter(farm -> uid.equals(farm.getUid()))
        .findFirst()
        .map(AgisFarmType::getFarmToPersonRelations)
        .map(AgisFarmToPersonRelations::getFarmToPersonRelation)
        .stream()
        .flatMap(List::stream)
        .findFirst()
        .map(AgisKtIdPRelationType::getKtIdP);

    if (responsibleKtIdP.isPresent()) {
      log.debug("Resolved responsible KtIdP for UID {}: {}", uid, responsibleKtIdP.get());
    } else {
      log.warn("Could not resolve responsible KtIdP for UID {}", uid);
    }

    return responsibleKtIdP;
  }

}
