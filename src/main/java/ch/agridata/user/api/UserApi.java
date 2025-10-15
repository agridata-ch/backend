package ch.agridata.user.api;

import ch.agridata.user.dto.BurDto;
import ch.agridata.user.dto.UidDto;
import java.util.List;
import lombok.NonNull;

/**
 * Declares operations for retrieving authorized BURs and UIDs. It provides a clean contract for external integration.
 *
 * @CommentLastReviewed 2025-08-25
 */
public interface UserApi {

  List<BurDto> getAuthorizedBurs(@NonNull String uid);

  List<UidDto> getAuthorizedUids(@NonNull String ktIdP);
}
