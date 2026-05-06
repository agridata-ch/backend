package ch.agridata.user.api;

import ch.agridata.user.dto.BurDto;
import ch.agridata.user.dto.UidDto;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;

/**
 * Declares operations for retrieving authorized BURs and UIDs. It provides a clean contract for external integration.
 *
 * @CommentLastReviewed 2026-05-06
 */
public interface UserApi {

  List<BurDto> getAuthorizedBurs(@NonNull String uid);

  List<UidDto> getAuthorizedUids(@Nullable String ktIdP, @Nullable String agateLoginId);

  List<UUID> getAdminUserIds();
}
