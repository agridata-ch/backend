package ch.agridata.agis.api;

import ch.agridata.agis.dto.AgisFarmType;
import ch.agridata.agis.dto.AgisPersonFarmResponseType;
import java.util.Optional;
import lombok.NonNull;

/**
 * Defines the interface for interacting with the AGIS API. It declares methods to fetch register data by person identifier, UID, or BUR,
 * returning structured farm and person data.
 *
 * @CommentLastReviewed 2025-10-02
 */
public interface AgisApi {

  AgisPersonFarmResponseType fetchRegisterDataForKtIdP(@NonNull String ktIdP);

  AgisPersonFarmResponseType fetchRegisterDataForUid(@NonNull String uid);

  Optional<AgisFarmType> fetchFarmForBur(@NonNull String bur);
}
