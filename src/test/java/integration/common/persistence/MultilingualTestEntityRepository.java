package integration.common.persistence;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.persistence.BaseSearchRepository;
import ch.agridata.common.persistence.SearchField;
import ch.agridata.common.persistence.SearchSpec;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MultilingualTestEntityRepository extends BaseSearchRepository<MultilingualTestEntity, Long> {

  public static final List<SearchField> FILTER_FIELDS = List.of(
      SearchField.translated("name"),
      SearchField.translated("description"),
      SearchField.simple("code")
  );

  public static final List<List<SearchField>> COMBINED_FIELDS = List.of(
      List.of(SearchField.translated("name"), SearchField.translated("description"))
  );

  public static final Map<String, SearchField> SORT_FIELDS = Map.of(
      "name", SearchField.translated("name"),
      "code", SearchField.simple("code")
  );

  public PageResponseDto<MultilingualTestEntity> search(ResourceQueryDto query, String category) {
    var spec = SearchSpec.builder()
        .filterFields(FILTER_FIELDS)
        .combinedFields(COMBINED_FIELDS)
        .sortFields(SORT_FIELDS);
    if (category != null) {
      spec.baseWhere("category = :category").baseParams(Map.of("category", category));
    }
    return findPageMultilingual(query, spec.build());
  }
}
