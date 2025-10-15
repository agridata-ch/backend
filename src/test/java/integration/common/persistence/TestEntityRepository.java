package integration.common.persistence;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.persistence.BaseSearchRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TestEntityRepository extends BaseSearchRepository<TestEntity, Long> {

  public static final List<String> FIELDS = List.of("firstName", "name", "description", "category");
  public static final List<List<String>> COMBINED_FIELDS = List.of(List.of("firstName", "name"));

  public PageResponseDto<TestEntity> searchEntitiesByCategory(ResourceQueryDto query, String category) {

    if (category == null) {
      return findPage(
          query,
          FIELDS,
          COMBINED_FIELDS
      );

    }
    return findPage(
        query,
        "category = :category",
        Map.of("category", category),
        FIELDS,
        COMBINED_FIELDS
    );
  }
}