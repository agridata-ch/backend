package ch.agridata.tvd.api;

import ch.agridata.tvd.dto.TvdEquidOwnerUidDto;
import java.util.List;
import lombok.NonNull;

/**
 * Defines the public API for accessing TVD equid owner legal unit information within the application.
 * Provides a method to fetch legal unit records for a given AGATE login ID.
 *
 * @CommentLastReviewed 2025-12-29
 */
public interface TvdApi {

  List<TvdEquidOwnerUidDto.Data> fetchEquidOwnerLegalUnits(@NonNull String agateLoginId);
}
