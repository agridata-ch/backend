package ch.agridata.uidregister.api;

import ch.agridata.uidregister.dto.UidRegisterOrganisationDto;
import ch.ech.xmlns.ech_0097._5.UidOrganisationIdCategorieType;
import java.math.BigInteger;

/**
 * Declares methods to retrieve organization details by a given UID or by the UID of the current authenticated user. It ensures consistent
 * contracts and error handling across services.
 *
 * @CommentLastReviewed 2025-08-25
 */
public interface UidRegisterServiceApi {

  /**
   * Retrieves the organisation details for the current user based on their UID.
   *
   * @return UidRegisterOrganisationDto containing the organisation details.
   * @throws UidMissingException         if the current user does not have a valid UID.
   * @throws NotFoundException           if no organisation is found for the current user's UID.
   * @throws ExternalWebServiceException if there is an error communicating with the UID web service.
   */
  UidRegisterOrganisationDto getByUidOfCurrentUser();

  /**
   * Retrieves the organisation details for the current user based on their UID.
   *
   * @return UidRegisterOrganisationDto containing the organisation details.
   * @throws UidMissingException         if the current user does not have a valid UID.
   * @throws NotFoundException           if no organisation is found for the current user's UID.
   * @throws ExternalWebServiceException if there is an error communicating with the UID web service.
   */
  UidRegisterOrganisationDto getByUid(UidOrganisationIdCategorieType category, BigInteger id);
}
