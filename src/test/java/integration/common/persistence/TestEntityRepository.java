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
public class TestEntityRepository extends BaseSearchRepository<TestEntity, Long> {

  public static final List<SearchField> FIELDS = List.of(
      SearchField.simple("firstName"),
      SearchField.simple("name"),
      SearchField.simple("description"),
      SearchField.simple("category")
  );
  public static final List<List<SearchField>> COMBINED_FIELDS = List.of(
      List.of(SearchField.simple("firstName"), SearchField.simple("name"))
  );
  public static final Map<String, SearchField> SORTABLE_FIELDS = Map.of(
      "firstName", SearchField.simple("firstName"),
      "name", SearchField.simple("name"),
      "description", SearchField.simple("description"),
      "category", SearchField.simple("category")
  );

  public PageResponseDto<TestEntity> searchEntitiesByCategory(ResourceQueryDto query, String category) {
    var spec = SearchSpec.builder()
        .searchableFields(FIELDS)
        .combinedFields(COMBINED_FIELDS)
        .sortableFields(SORTABLE_FIELDS);
    if (category != null) {
      spec.baseWhere("category = :category").baseParams(Map.of("category", category));
    }
    return findPage(query, spec.build());
  }
}
