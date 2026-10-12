package ch.agridata.burregister.api;

import ch.agridata.burregister.dto.BurRegisterOrganisationDto;

/**
 * Declares methods to retrieve organization details from the BUR by a given UID or by the UID of the current authenticated user.
 *
 * @CommentLastReviewed 2026-10-09
 */
public interface BurRegisterServiceApi {

  /**
   * Retrieves the organisation details for the current user based on their UID.
   *
   * @return BurRegisterOrganisationDto containing the organisation details.
   * @throws UidClaimMissingException    if the current user does not have a valid UID.
   * @throws NotFoundException           if no organisation is found for the current user's UID.
   * @throws ExternalWebServiceException if there is an error communicating with the BUR web service.
   */
  BurRegisterOrganisationDto getByUidOfCurrentUser();

  /**
   * Retrieves the organisation details for any UID.
   *
   * @param uid full UID including the CHE prefix, e.g. {@code CHE113614519}
   * @return BurRegisterOrganisationDto containing the organisation details.
   * @throws ValidationException         if the UID is not 'CHE' followed by 9 digits.
   * @throws NotFoundException           if no organisation is found for the given UID.
   * @throws ExternalWebServiceException if there is an error communicating with the BUR web service.
   */
  BurRegisterOrganisationDto getByUid(String uid);
}
