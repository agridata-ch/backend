package ch.agridata.product.api;

import ch.agridata.product.dto.DataSourceSystemDto;
import java.util.List;

/**
 * Declares operations for retrieving data source systems together with their data provider. It ensures a stable
 * contract for other modules (e.g. the health module, which reports the reachability of each system).
 *
 * @CommentLastReviewed 2026-08-11
 */
public interface DataSourceSystemApi {
  List<DataSourceSystemDto> getDataSourceSystems();
}
